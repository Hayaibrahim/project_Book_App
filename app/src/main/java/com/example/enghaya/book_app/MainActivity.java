package com.example.enghaya.book_app;

import android.content.Context;
import android.database.DataSetObserver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {
    final String search = "https://www.googleapis.com/books/v1/volumes?q=acricket";
    TextView text;
    EditText meditText;
    BookAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton mimageButton = (ImageButton) findViewById(R.id.imageButton2);
        meditText = (EditText) findViewById(R.id.editText);
        ListView mlistView = (ListView) findViewById(R.id.list);
        text = (TextView) findViewById(R.id.textView);

        adapter = new BookAdapter(this, -1);
        mlistView.setAdapter(adapter);
        mimageButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (isInternetConnectionAvailable()) {
                    BooksAsyncTask books = new BooksAsyncTask();
                    new BooksAsyncTask().execute();
                } else {
                    Toast.makeText(MainActivity.this, R.string.hello_blank_fragment,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        if (savedInstanceState != null) {
            Book[] book = (Book[]) savedInstanceState.getParcelableArray(search);
            adapter.addAll(book);
        }
    }

    private boolean isInternetConnectionAvailable() {
        //connectivity
        ConnectivityManager cconnect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo net = cconnect.getActiveNetworkInfo();
        return net.isConnectedOrConnecting();
    }

    public void update(List<Book> books) {
        //I use if statment beacouse
        // cheack a book is found or not  found
        if (books.isEmpty()) {
            text.setVisibility(View.VISIBLE);
            text.setText("The book was not found, kindly search for another book.");

        } else {
            text.setVisibility(View.GONE);
        }
        adapter.clear();
        adapter.addAll(books);
    }

    private String url() {
        return meditText.getText().toString();
    }

    public String Http() {
        final String searchbooks = "https://www.googleapis.com/books/v1/volumes?q=a";
        String inputforuser = url().trim().replaceAll("\\+", "+");
        String url = searchbooks + inputforuser;

        return url;
    }

    public class BooksAsyncTask extends AsyncTask<URL, Void, List<Book>> {
        @Override
        protected List<Book> doInBackground(URL... urls) {
            URL url = forUrl(Http());
            String Josn = "   ";
            try {
                Josn = useHttp(url);

            } catch (IOException e) {
                e.printStackTrace();
            }
            List<Book> book = Bookjosn(Josn);
            return book;
        }

        protected void onPostExecute(List<Book> result) {
            if (result == null) {
                return;
            }
            update(result);
        }

        private URL forUrl(String http) {
            try {

                return new URL(http);
            } catch (MalformedURLException m) {
                m.printStackTrace();
                return null;
            }
        }
    }

    private String useHttp(URL url) throws IOException {

        InputStream stream = null;
        HttpsURLConnection connection = null;
        String result = null;
        if (url == null) {
            return result;
        }
        try {
            connection = (HttpsURLConnection) url.openConnection();
            // For this use case, set HTTP method to GET.
            connection.setRequestMethod("GET");
            // Timeout for reading InputStream arbitrarily set to 10000.
            connection.setReadTimeout(10000);
            // Timeout for connection.connect() arbitrarily set to 150000.
            connection.setConnectTimeout(15000);
            // Open communications link (network traffic occurs here).
            connection.connect();
            // Already true by default but setting just in case; needs to be true since this request
            // is carrying an input (response) body.

            if (connection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
                result = readinputstream(stream);
            } else {
                Log.e("MainActivity", "try again" + connection.getResponseCode());

            }
        } catch (IOException e) {
            e.printStackTrace();
            // Retrieve the response body as an InputStream.
            if (stream != null) {
                // Converts Stream to String with max length of 500.
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            if (stream != null) {
                stream.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
        return result;
    }

    private List<Book> Bookjosn(String json) {

        if (json == null) {
            return null;
        }
        List<Book> book = Query.extractBooks(json);
        return book;
    }

    public String readinputstream(InputStream input) throws IOException {

        StringBuilder builder = new StringBuilder();
        if (input != null) {
            InputStreamReader reader = new InputStreamReader(input, Charset.forName("UTF-8"));
            BufferedReader buffer = new BufferedReader(reader);
            String Linebyline = buffer.readLine();
            while (Linebyline != null) {
                builder.append(Linebyline);
                Linebyline = buffer.readLine();
            }

            return builder.toString();
        }
        Log.i("result", builder.toString());
        JSONObject json = null;
        try {
            json = new JSONObject(builder.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (json.has("items")) {
            JSONArray arrayObject = null;
            try {
                arrayObject = json.getJSONArray("items");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            List<String> authors;
            for (int i = 0; i < arrayObject.length(); i++) {
                JSONObject object = null;
                try {
                    object = arrayObject.getJSONObject(i).getJSONObject("volumeInfo");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                authors = new ArrayList<>();
                try {
                    JSONArray authorsList = object.getJSONArray("authors");
                    if (authors != null)
                        for (int j = 0; j < authorsList.length(); j++) {
                            authors.add(authorsList.getString(j));
                        }
                } catch (JSONException e) {
                    authors.add(MainActivity.this.getString(R.string.notapplicable));
                }
            }
        }
        return null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Book[] books = new Book[adapter.getCount()];
        for (int i = 0; i < books.length; i++) {
            books[i] = (Book) adapter.getItem(i);
        }
        outState.putParcelableArray(search, (Parcelable[]) books);
    }


}