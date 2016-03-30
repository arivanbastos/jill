package br.com.arivanbastos.signalcaptor.views;

import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.arivanbastos.signalcaptor.MainActivity;
import br.com.arivanbastos.signalcaptor.R;
import br.com.arivanbastos.signalcaptor.location.BaseLocationMethod;
import br.com.arivanbastos.signalcaptor.location.LocationMethodParameter;
import br.com.arivanbastos.signalcaptor.location.exceptions.InvalidParameterValueException;
import br.com.arivanbastos.signalcaptor.utils.AndroidUtil;

public class AddMethodDialog extends Dialog implements View.OnClickListener, AdapterView.OnItemSelectedListener
{
    private MainActivity mainActivity;
    private List<BaseLocationMethod> methods;

    public AddMethodDialog(MainActivity mainActivity)
    {
        super(mainActivity);
        this.mainActivity = mainActivity;

        setContentView(R.layout.new_method);
        setTitle("New Location Method");
        setCancelable(true);

        // Add/cancel buttons.
        Button button = (Button) this.findViewById(R.id.cancelButton);
        button.setOnClickListener(this);
        button = (Button) this.findViewById(R.id.createButton);
        button.setOnClickListener(this);

        // Available location methods.
        methods = BaseLocationMethod.getRegisteredMethods();
        List<String> methodNames = new ArrayList<String>();
        methodNames.add("Select...");
        for (BaseLocationMethod method : methods)
            methodNames.add(method.getName());

        // Methods spinner.
        Spinner spinner = (Spinner) findViewById(R.id.methodsSpinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_spinner_item, methodNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    public void show()
    {
        methods = BaseLocationMethod.getRegisteredMethods();

        super.show();
    }

    /**
     * Displays the selected method parameters for user configuration.
     * @param method
     */
    public void loadMethodParameters(BaseLocationMethod method)
    {
        LinearLayout container = (LinearLayout)findViewById(R.id.parametersContainer);
        container.removeAllViews();

        // Loop trough location method parameters adding them to
        // parameters container.
        Log.i("AddMethodDialog", "loadMethodParameters() for " + method.getName());
        for (LocationMethodParameter p : method.getParameters())
        {
            LinearLayout layout = new LinearLayout(mainActivity);

            TextView label = new TextView(mainActivity);
            label.setText(p.getName());

            EditText value = new EditText(mainActivity);
            value.setText(p.getDefaultValue().toString());
            value.setTag(p.getName());

            layout.addView(label);
            layout.addView(value);

            Log.i("AddMethodDialog", "  adding " + p.getName());
            container.addView(layout);
        }
    }

    /**
     * Creates a LocationMethod object from form.
     */
    public BaseLocationMethod createLocationMethod() throws Exception
    {
        Spinner spinner = (Spinner) findViewById(R.id.methodsSpinner);
        if (spinner.getSelectedItemPosition()==0)
            throw new Exception("Select a location method!");

        BaseLocationMethod method = methods.get(spinner.getSelectedItemPosition()-1);
        LinearLayout container = (LinearLayout)findViewById(R.id.parametersContainer);
        int i = 0;
        for (LocationMethodParameter p : method.getParameters())
        {
            List<View> views = AndroidUtil.getViewsByTag(container, p.getName());
            EditText parameterValue = (EditText)views.get(0);

            if (parameterValue.getText().toString().isEmpty())
                throw new Exception("Parameter "+p.getName()+" is required.");

            try {
                Log.i("AddMethodDialog", "Setting value for "+p.getName()+": " + parameterValue.getText().toString());
                method.setParameterValue(p.getName(), parameterValue.getText().toString());
            } catch (InvalidParameterValueException e)
            {
                throw new Exception("Invalid value '"+parameterValue.getText().toString()+"' for parameter " + p.getName()+".");
            }
        }

        return method;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.i("AddMethodDialog", "onItemSelected() " + position);
        if (position==0)
        {
            LinearLayout container = (LinearLayout)findViewById(R.id.parametersContainer);
            container.removeAllViews();
        }
        else {
            BaseLocationMethod method = methods.get(position-1);
            loadMethodParameters(method);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onClick(View v) {

        if (v.getId()==R.id.cancelButton)
        {
            dismiss();
            //hide();
        }
        else
        {
            try
            {
                BaseLocationMethod method = createLocationMethod();

                mainActivity.addLocationMethod(method);
                dismiss();
                //hide();
            }
            catch (Exception e)
            {
                Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}
