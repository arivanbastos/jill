package br.com.arivanbastos.signalcaptor.views;

import android.app.Dialog;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import br.com.arivanbastos.signalcaptor.MainActivity;
import br.com.arivanbastos.signalcaptor.R;
import br.com.arivanbastos.jillcore.models.map.rooms.Room;
import br.com.arivanbastos.jillcore.models.math.Point2;

public class CreatePointDialog extends Dialog implements View.OnClickListener
{
    private MainActivity mainActivity;
    private Spinner roomsSpinner;

    public CreatePointDialog(MainActivity mainActivity) {
        super(mainActivity);

        this.mainActivity = mainActivity;

        setContentView(R.layout.new_point);
        setTitle("New Map Point");
        setCancelable(true);

        // Buttons listeners.
        Button button = (Button) this.findViewById(R.id.cancelButton);
        button.setOnClickListener(this);
        button = (Button) this.findViewById(R.id.createButton);
        button.setOnClickListener(this);

        // Populates Room Spinner with map rooms.
        roomsSpinner = (Spinner) findViewById(R.id.roomsSpinner);
        List<String> rooms = new ArrayList<String>();
        for (Room r : mainActivity.map.getRooms())
        {
            // Ignore walls.
            if (!r.getId().startsWith("wall"))
                rooms.add(r.getId());
        }
        java.util.Collections.sort(rooms);

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_spinner_item, rooms);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomsSpinner.setAdapter(spinnerArrayAdapter);

        // Hides Room Spinner if gridType is Global.
        findViewById(R.id.roomsLayout).setVisibility(mainActivity.getMapView().getGridType() == MapView.GRID_TYPE_GLOBAL ? View.INVISIBLE : View.VISIBLE);
    }

    public void onClick(View v)
    {
        if (v.getId()==R.id.cancelButton)
        {
            dismiss();
        }
        else
        {
            try
            {
                EditText xText = (EditText)this.findViewById(R.id.xText);
                EditText yText = (EditText)this.findViewById(R.id.yText);

                if (xText.getText().toString().isEmpty() || yText.getText().toString().isEmpty())
                    throw new Exception("Position is required.");

                Double x = Double.parseDouble(xText.getText().toString());
                Double y = Double.parseDouble(yText.getText().toString());

                String room = null;
                if (findViewById(R.id.roomsLayout).getVisibility()==View.VISIBLE)
                    room = roomsSpinner.getSelectedItem().toString();

                mainActivity.createPoint(new Point2.Double(x,y), room);
                dismiss();
            }
            catch (Exception e)
            {
                Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
