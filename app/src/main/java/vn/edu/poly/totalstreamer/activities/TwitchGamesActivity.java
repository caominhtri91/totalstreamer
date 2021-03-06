package vn.edu.poly.totalstreamer.activities;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import vn.edu.poly.totalstreamer.R;
import vn.edu.poly.totalstreamer.customAdapter.GameListAdapter;
import vn.edu.poly.totalstreamer.customAdapter.GridSpacingItemDecoration;
import vn.edu.poly.totalstreamer.entities.GameEntity;
import vn.edu.poly.totalstreamer.extras.CustomMethods;
import vn.edu.poly.totalstreamer.myLog.L;
import vn.edu.poly.totalstreamer.network.VolleySingleton;

import static vn.edu.poly.totalstreamer.extras.Keys.KEY_BOX;
import static vn.edu.poly.totalstreamer.extras.Keys.KEY_GAME;
import static vn.edu.poly.totalstreamer.extras.Keys.KEY_LARGE;
import static vn.edu.poly.totalstreamer.extras.Keys.KEY_NAME;
import static vn.edu.poly.totalstreamer.extras.Keys.KEY_TOP;
import static vn.edu.poly.totalstreamer.extras.SpecialChars.KEY_AND;
import static vn.edu.poly.totalstreamer.extras.SpecialChars.KEY_QUESTION;
import static vn.edu.poly.totalstreamer.extras.UrlEndPoints.URL_API_KEY;
import static vn.edu.poly.totalstreamer.extras.UrlEndPoints.URL_LIMIT;
import static vn.edu.poly.totalstreamer.extras.UrlEndPoints.URL_PARAM;
import static vn.edu.poly.totalstreamer.extras.UrlEndPoints.URL_TWITCH;

public class TwitchGamesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    RecyclerView gameList;
    List<GameEntity> games;
    GameListAdapter gameListAdapter;

    VolleySingleton volleySingleton;
    RequestQueue requestQueue;

    public static String requestUrl() {
        return URL_TWITCH + URL_PARAM + KEY_QUESTION + URL_LIMIT + "50" + KEY_AND + URL_API_KEY;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitch_games);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        games = new ArrayList<>();
        gameList = (RecyclerView) findViewById(R.id.gameList);

        volleySingleton = VolleySingleton.getsInstance();
        requestQueue = VolleySingleton.getRequestQueue();
        sendJSONRequest();

        gameList.setLayoutManager(new GridLayoutManager(this, 2));
        gameList.addItemDecoration(new GridSpacingItemDecoration(2, CustomMethods.dpToPx(10), true));
        gameListAdapter = new GameListAdapter(this);
        gameList.setAdapter(gameListAdapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_services) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_account) {
            Intent i = new Intent(this, AccountActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_licenses) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TwitchGamesActivity.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialogView = inflater.inflate(R.layout.custom_licenses, null);
            dialogBuilder.setView(dialogView);

            AlertDialog b = dialogBuilder.create();
            b.show();
        } else if (id == R.id.nav_about) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(TwitchGamesActivity.this);
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View dialogView = inflater.inflate(R.layout.custom_about, null);
            dialogBuilder.setView(dialogView);

            TextView textView2 = (TextView) dialogView.findViewById(R.id.textView2);

            textView2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                            "mailto", "tricmps04382@fpt.edu.vn", null));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Hello Mr. Tri. I have something to say to you...");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Lorem Isum");
                    startActivity(Intent.createChooser(emailIntent, "Send email..."));
                }
            });

            AlertDialog b = dialogBuilder.create();
            b.show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendJSONRequest() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, requestUrl(), null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                games = parseJsonResponse(response);
                gameListAdapter.setGamesList(games);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                L.m(error.toString());
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    private List<GameEntity> parseJsonResponse(JSONObject response) {
        List<GameEntity> list = new ArrayList<>();

        if (response == null && response.length() == 0) {
            return null;
        }

        try {
            JSONArray top = response.getJSONArray(KEY_TOP);

            for (int i = 0; i < top.length(); i++) {
                JSONObject jsonObject = top.getJSONObject(i);
                JSONObject game = jsonObject.getJSONObject(KEY_GAME);
                JSONObject imgBox = game.getJSONObject(KEY_BOX);
                String gameName = game.getString(KEY_NAME);
                String urlImg = imgBox.getString(KEY_LARGE);
                GameEntity gameEntity = new GameEntity(urlImg, gameName);
                list.add(gameEntity);
            }
        } catch (JSONException e) {
            L.m(e.toString());
        }

        return list;
    }
}
