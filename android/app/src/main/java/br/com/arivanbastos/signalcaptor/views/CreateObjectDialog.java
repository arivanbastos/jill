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
import br.com.arivanbastos.jillcore.models.map.MapObject;
import br.com.arivanbastos.jillcore.models.map.rooms.Room;

public class CreateObjectDialog extends Dialog implements View.OnClickListener
{
    private MainActivity mainActivity;
    private Spinner roomsSpinner;

    public CreateObjectDialog(MainActivity mainActivity) {
        super(mainActivity);

        this.mainActivity = mainActivity;

        setContentView(R.layout.new_object);
        setTitle("New Map Object");
        setCancelable(true);

        // Buttons listeners.
        Button button = (Button) this.findViewById(R.id.cancelButton);
        button.setOnClickListener(this);
        button = (Button) this.findViewById(R.id.createButton);
        button.setOnClickListener(this);
        button = (Button) this.findViewById(R.id.removeButton);
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

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(mainActivity, android.R.layout.simple_spinner_item, rooms);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roomsSpinner.setAdapter(spinnerArrayAdapter);

        // Hides Room Spinner if gridType is Global.
        findViewById(R.id.roomsLayout).setVisibility(mainActivity.getMapView().getGridType() == MapView.GRID_TYPE_GLOBAL ? View.INVISIBLE : View.VISIBLE);

        findViewById(R.id.removeButton).setVisibility(View.INVISIBLE);
    }

    public void setObject(MapObject object)
    {
        EditText xText = (EditText)this.findViewById(R.id.xText);
        EditText yText = (EditText)this.findViewById(R.id.yText);
        EditText zText = (EditText)this.findViewById(R.id.zText);
        EditText idText = (EditText)this.findViewById(R.id.idText);

        xText.setText(object.getPosition().getX()+"");
        yText.setText(object.getPosition().getY()+"");
        //zText.setText(object.getPosition().getZ()+"");
        zText.setText("1");

        idText.setText(object.getId());

        findViewById(R.id.removeButton).setVisibility(View.VISIBLE);
    }

    public void onClick(View v)
    {
        if (v.getId()==R.id.cancelButton)
        {
            dismiss();
        }
        else if (v.getId()==R.id.removeButton)
        {
            EditText idText = (EditText)this.findViewById(R.id.idText);
            String id   = idText.getText().toString();
            mainActivity.removeObject(id);

            dismiss();
        }
        else
        {
            try
            {
                EditText xText = (EditText)this.findViewById(R.id.xText);
                EditText yText = (EditText)this.findViewById(R.id.yText);
                EditText zText = (EditText)this.findViewById(R.id.zText);
                EditText idText = (EditText)this.findViewById(R.id.idText);

                String x    = xText.getText().toString();
                String y    = yText.getText().toString();
                String z    = zText.getText().toString();
                String id   = idText.getText().toString();

                if (x.isEmpty())
                    throw new Exception("X value is required.");

                if (y.isEmpty())
                    throw new Exception("Y value is required.");

                if (z.isEmpty())
                    throw new Exception("Z value is required.");

                if (id.isEmpty())
                    throw new Exception("ID is required.");

                String room = null;
                if (findViewById(R.id.roomsLayout).getVisibility()==View.VISIBLE)
                    room = roomsSpinner.getSelectedItem().toString();

                mainActivity.saveObject(Float.valueOf(x), Float.valueOf(y), Float.valueOf(z), id, room);
                dismiss();
            }
            catch (Exception e)
            {
                Toast.makeText(mainActivity, e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
