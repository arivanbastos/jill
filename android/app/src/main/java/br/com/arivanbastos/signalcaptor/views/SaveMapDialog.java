package br.com.arivanbastos.signalcaptor.views;

import android.app.Dialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import br.com.arivanbastos.signalcaptor.MainActivity;
import br.com.arivanbastos.signalcaptor.R;

public class SaveMapDialog extends Dialog implements View.OnClickListener
{
    private MainActivity mainActivity;
    public SaveMapDialog(MainActivity mainActivity)
    {
        super(mainActivity);

        this.mainActivity = mainActivity;

        setContentView(R.layout.save_map);
        setTitle("Save Map");
        setCancelable(true);

        Button button = (Button) this.findViewById(R.id.cancelButton);
        button.setOnClickListener(this);

        button = (Button) this.findViewById(R.id.saveButton);
        button.setOnClickListener(this);
    }

    public void onClick(View v) {

        if (v.getId()==R.id.cancelButton)
        {
            dismiss();
        }
        else
        {
            try
            {
                EditText fileNameText = (EditText)this.findViewById(R.id.fileNameText);
                String fileName = fileNameText.getText().toString();

                if (fileName.isEmpty())
                    throw new Exception("File name is required.");

                mainActivity.saveMap(fileName);
                dismiss();
            }
            catch (Exception e)
            {
                Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
