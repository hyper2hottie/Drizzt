package realms.drizzt.fragmentactivity;

import realms.drizzt.mainactivity.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TestFragment extends Fragment {

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
		
		//Inflate the view to return
		View toReturn = inflater.inflate(R.layout.test_fragment, container, false);
		
		//Get arguments
		Bundle args = getArguments();
		
		//Check if we are setting anythings
		if(args != null)
		{
			//Check for setting text
			if(args.getString("text") != null)
				setText(args.getString("text"), (TextView)toReturn.findViewById(R.id.textView));
        
			//Check for setting the color
			int color = args.getInt("color", 0);
			((LinearLayout)toReturn.findViewById(R.id.testFragmentLayout)).setBackgroundColor(color);
		}
		
        return toReturn;
    }
		

	/**
	 * This function is used to set the text that will be displayed in the fragment.
	 * @param text - The text to put into the fragment.
	 * @param view - The text view to set the text on.
	 */
	public void setText(String text, TextView view)
	{
		view.setText(text);		
	}
}
