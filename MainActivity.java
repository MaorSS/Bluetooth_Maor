package com.example.bluetooth_maor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.Button;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


    public class MainActivity extends AppCompatActivity {
        private Button search; // כפתור החיפוש
        private Button connect; // כפתור ההתחברות
        private ListView listView; // הרשימה שמציגה את הבלוטוסים שאנחנו עברנו את תהליך ה pairing איתם
        private BluetoothAdapter mBTAdapter; // מתאם בלוטוס
        private static final int BT_ENABLE_REQUEST = 10; // זה הקוד להפעלת הבלוטוס זה עוזר לנו מתי שנצטרך להשתמש בפונקציית ההפעלה זה יהיה פרמטר
        private static final int SETTINGS = 20;
        private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // מספר תעודת זהות של המודול בלוטוס שלנו - HC-05
        private int mBufferSize = 50000; // גודל המערך אני חושב
        public static final String DEVICE_EXTRA = "com.example.Bluetooth_Maor.SOCKET"; // אני חושב שזה המיקום של המתאם בלוטוס
        public static final String DEVICE_UUID = "com.example.Bluetooth_Maor.uuid"; // מיקום תעודת הזהות של המכשיר שאנחנו משתמשים בו
        private static final String DEVICE_LIST = "com.example.Bluetooth_Maor.devicelist"; // רשימת המכשירים
        private static final String DEVICE_LIST_SELECTED = "com.example.Bluetooth_Maor.devicelistselected"; // רשימת המכשירים שנבחרו
        public static final String BUFFER_SIZE = "com.example.Bluetooth_Maor.buffersize"; // גודל המערך
        private static final String TAG = "BlueTest5-MainActivity"; // שם הטאג?

  /*
  הפונקציה oncreate היא כמו הפונקציה Main ב C# וJAVA
  בתוך הפונקציית מיין שלנו אנחנו נותנים לכל הכפתורים והרשימות שלנו את הת.ז שלהם כדי שנוכל
  לבצע איתם או עליהם פעולות



   */


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);



            search = (Button) findViewById(R.id.search); // מספר תעודת זהות לכפתור החיפוש בלוטוסים
            connect = (Button) findViewById(R.id.connect); // מספר תעודת זהות לכפתור ההתחברות אל הבלוטוס
            listView = (ListView) findViewById(R.id.listview); // מספר תעודת זהות לרשימת בלוטוסים

            if (savedInstanceState != null) // אני מניח שמשהו נשמר פה אבל אין לי מושג מה זה האובייקט הזה ומה הוא מכיל
            {
                ArrayList<BluetoothDevice> list = savedInstanceState.getParcelableArrayList(DEVICE_LIST); // יוצר מערך בשם ליסט של כל המכשירי בלוטוס
                if (list != null) { // בודק האם המערך לא ריק
                    initList(list); // לא סגור על זה וזה חוזר כמה פעמים
                    MyAdapter adapter = (MyAdapter) listView.getAdapter(); //
                    int selectedIndex = savedInstanceState.getInt(DEVICE_LIST_SELECTED); //  מכניס את המכשיר שנבחר למערך?
                    if (selectedIndex != -1) { // לא יודע
                        adapter.setSelectedIndex(selectedIndex);
                        connect.setEnabled(true);
                    }
                } else {
                    initList(new ArrayList<BluetoothDevice>());
                }

            } else {
                initList(new ArrayList<BluetoothDevice>());
            }
            search.setOnClickListener(new View.OnClickListener() { // מטפל אירוע קליק

                @Override
                public void onClick(View arg0) { // מטפל אירוע
                    mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // לוקח את המתאם בלוטוס בשם mBTAdapter ומכניס לתוכו את המתאם בלוטוס ברירת המחדל

                    if (mBTAdapter == null) { // בודק האם המתאם בלוטוס ברירת מחדל ריק
                        Toast.makeText(getApplicationContext(), "Bluetooth not found", Toast.LENGTH_SHORT).show(); // אם הוא ריק הוא מציג הודעת טואסט שאומרת שהבלוטוס לא נמצא
                    } else if (!mBTAdapter.isEnabled()) { // במידה ויש מתאם בלוטוס ברירת מחדל אבל הוא כבוי אז מדליקים אותו
                        Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); // בשביל להדליק אותו צריך לדבר עם המערכת אנדרואיד ולהעביר בקשה אל המערכת ועושים את זה בעזרת Intent
                        // בנוסף אנחנו מבקשים Premmision בשביל להדליק את הבלוטוס
                        startActivityForResult(enableBT, BT_ENABLE_REQUEST); // הפונקציה startActivityForResult יודעת לקחת Intent וקוד בקשה וככה היא מפעילה את הבלוטוס
                    } else {
                        new SearchDevices().execute(); // במידה והמשתמש לא ידליק את הבלוטוס
                    }
                }
            });

            connect.setOnClickListener(new View.OnClickListener() { // מאזין אירוע מסוג קליק עבור כפתור ההתחברות

                @Override
                public void onClick(View arg0) { // מטפל אירוע מסוג קליק עבור כפתור ההתחברות
                    BluetoothDevice device = ((MyAdapter) (listView.getAdapter())).getSelectedItem(); // מחפש במערך ליסט את המכשיר?
                    Intent intent = new Intent(getApplicationContext(), controlling.class); // getApplicationContext() אין לי מושג מה זה
                    intent.putExtra(DEVICE_EXTRA, device);
                    intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                    intent.putExtra(BUFFER_SIZE, mBufferSize);
                    startActivity(intent);
                }
            });



        }

        protected void onPause() { // פונקציית פסק זמן
// TODO Auto-generated method stub
            super.onPause();
        }

        @Override
        protected void onStop() { // פונקציית עצירה
// TODO Auto-generated method stub
            super.onStop();
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            switch (requestCode) {
                case BT_ENABLE_REQUEST: // בודק האם הקוד REQUEST שווה לresultcode ובכך מבין אם נעשתה בקשה והיא זהה לערך של הדלקת הבלוטוס
                    if (resultCode == RESULT_OK) {
                        msg("Bluetooth Enabled successfully");
                        new SearchDevices().execute();
                    } else {
                        msg("Bluetooth couldn't be enabled");
                    }

                    break;
                case SETTINGS: //אולי אם יש הגדרות והן השתנו אני מניח
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                    String uuid = prefs.getString("prefUuid", "Null");
                    mDeviceUUID = UUID.fromString(uuid);
                    Log.d(TAG, "UUID: " + uuid);
                    String bufSize = prefs.getString("prefTextBuffer", "Null");
                    mBufferSize = Integer.parseInt(bufSize);

                    String orientation = prefs.getString("prefOrientation", "Null");
                    Log.d(TAG, "Orientation: " + orientation);
                    if (orientation.equals("Landscape")) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    } else if (orientation.equals("Portrait")) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    } else if (orientation.equals("Auto")) {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                    }
                    break;
                default:
                    break;
            }
            super.onActivityResult(requestCode, resultCode, data);
        }


        private void msg(String str) { // פונקציה של הודעה שמקבלת פרמטר סטרינג ומראה את str
            Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
        }


// פונקציה שכנראה מאזינה נתונים לתוך המערך ליסט של המכשירים
        private void initList(List<BluetoothDevice> objects) {
            final MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.list_item, R.id.lstContent, objects);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    adapter.setSelectedIndex(position);
                    connect.setEnabled(true);
                }
            });
        }


        private class SearchDevices extends AsyncTask<Void, Void, List<BluetoothDevice>> {

            @Override
            protected List<BluetoothDevice> doInBackground(Void... params) {
                Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
                List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
                for (BluetoothDevice device : pairedDevices) {
                    listDevices.add(device);
                }
                return listDevices;

            }

            @Override
            protected void onPostExecute(List<BluetoothDevice> listDevices) {
                super.onPostExecute(listDevices);
                if (listDevices.size() > 0) {
                    MyAdapter adapter = (MyAdapter) listView.getAdapter();
                    adapter.replaceItems(listDevices);
                } else {
                    msg("No paired devices found, please pair your serial BT device and try again");
                }
            }

        }

        /**
         * Custom adapter to show the current devices in the list. This is a bit of an overkill for this
         * project, but I figured it would be good learning
         * Most of the code is lifted from somewhere but I can't find the link anymore
         * @author ryder
         *
         */
        private class MyAdapter extends ArrayAdapter<BluetoothDevice> {
            private int selectedIndex;
            private Context context;
            private int selectedColor = Color.parseColor("#abcdef");
            private List<BluetoothDevice> myList;

            public MyAdapter(Context ctx, int resource, int textViewResourceId, List<BluetoothDevice> objects) {
                super(ctx, resource, textViewResourceId, objects);
                context = ctx; // לא יודע מה context בתור אובייקט מכיל ועושה
                myList = objects; // רשימת מכשירים אני מניח
                selectedIndex = -1; // כנראה המיקום של האינדקס בתוך המערך של הרשימה
            }

            public void setSelectedIndex(int position) {
                selectedIndex = position;
                notifyDataSetChanged();
            }

            public BluetoothDevice getSelectedItem() {
                return myList.get(selectedIndex);
            }

            @Override
            public int getCount() {
                return myList.size();
            }

            @Override
            public BluetoothDevice getItem(int position) {
                return myList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            private class ViewHolder {
                TextView tv;
            }

            public void replaceItems(List<BluetoothDevice> list) {
                myList = list;
                notifyDataSetChanged();
            }

            public List<BluetoothDevice> getEntireList() {
                return myList;
            }

            // כנראה מחזיר את כל הרשימה של המכשירי בלוטוסים
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View vi = convertView;
                ViewHolder holder;
                if (convertView == null) {
                    vi = LayoutInflater.from(context).inflate(R.layout.list_item, null);
                    holder = new ViewHolder();

                    holder.tv = (TextView) vi.findViewById(R.id.lstContent);

                    vi.setTag(holder);
                } else {
                    holder = (ViewHolder) vi.getTag();
                }

                if (selectedIndex != -1 && position == selectedIndex) {
                    holder.tv.setBackgroundColor(selectedColor);
                } else {
                    holder.tv.setBackgroundColor(Color.WHITE);
                }
                BluetoothDevice device = myList.get(position);
                holder.tv.setText(device.getName() + "\n " + device.getAddress());

                return vi;
            }

        }

        // עושה משהו עם המניו
        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
// Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        }


        // זה סרוויס שיצרתי והוא מפעיל את המוזיקה במידה ולחצנו עליו בתוך הMENU ומפסיק במידה ואנחנו עוצרים אותו בנוסף על כך הוא גם יכול לסגור את האפליקציה
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                // אני לא יודע מה הקייס הזה עושה
                case R.id.action_settings:
                    Intent intent = new Intent(MainActivity.this, PreferencesActivity.class);
                    startActivityForResult(intent, SETTINGS);
                    break;
                case R.id.startmusic:
                    Toast.makeText(this,"The Music has been enabled",Toast.LENGTH_LONG).show();
                    startService(new Intent(this,MusicService.class));
                    break;
                case R.id.stopmusic:
                    Toast.makeText(this,"The Music has been disabled",Toast.LENGTH_LONG).show();
                    stopService(new Intent(this,MusicService.class));
                    break;
                case R.id.itemNext:
                    Toast.makeText(this,"Exiting the application!",Toast.LENGTH_LONG).show();
                    finish();
                    break;


            }
            return super.onOptionsItemSelected(item);
        }
    }
