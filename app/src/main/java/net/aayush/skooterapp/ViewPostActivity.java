package net.aayush.skooterapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import net.aayush.skooterapp.data.Comment;
import net.aayush.skooterapp.data.Post;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ViewPostActivity extends BaseActivity {

    private static final String LOG_TAG = ViewPostActivity.class.getSimpleName();
    protected List<Comment> mCommentsList = new ArrayList<Comment>();
    protected ArrayAdapter<Comment> mCommentsAdapter;
    protected ListView mListComments;
    protected Post mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);

        activateToolbarWithHomeEnabled("");

        Intent intent = getIntent();
        mPost = (Post) intent.getSerializableExtra(SKOOTER_POST);
        List<Post> postList = new ArrayList<Post>();
        postList.add(mPost);

        final LinearLayout flagView = (LinearLayout) findViewById(R.id.flag_view);
        final LinearLayout deleteView = (LinearLayout) findViewById(R.id.delete_view);
        final TextView typeIdView = (TextView) findViewById(R.id.type_id);
        final TextView typeView = (TextView) findViewById(R.id.type);

        ArrayAdapter<Post> postAdapter = new PostAdapter(this, R.layout.list_view_post_row, postList, true, flagView, deleteView, typeIdView, typeView);
        ListView listPosts = (ListView) findViewById(R.id.list_posts);
        listPosts.setAdapter(postAdapter);

        String tag = "load_comments";
        mCommentsAdapter = new CommentsAdapter(this, R.layout.list_view_comment_post_row, mCommentsList, true, flagView, deleteView, typeIdView, typeView);
        mListComments = (ListView) findViewById(R.id.list_comments);
        mListComments.setAdapter(mCommentsAdapter);

        getCommentsForPostId(mPost, userId);

        Button commentBtn = (Button) findViewById(R.id.commentSkoot);
        final TextView commentText = (TextView) findViewById(R.id.commentText);
        final int postId = mPost.getId();

        commentBtn.setOnClickListener(new View.OnClickListener() {
            String tag = "post_comment";

            @Override
            public void onClick(View v) {
                if (commentText.getText().length() > 0 && commentText.getText().length() < 250) {
                    String url = "http://skooter.elasticbeanstalk.com/comment";
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("user_id", Integer.toString(BaseActivity.userId));
                    params.put("content", commentText.getText().toString());
                    params.put("location_id", Integer.toString(BaseActivity.locationId));
                    params.put("post_id", Integer.toString(postId));

                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d(LOG_TAG, response.toString());
                            commentText.setText("");
                            Toast.makeText(ViewPostActivity.this, "Woot! Comment posted!", Toast.LENGTH_SHORT).show();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.d(LOG_TAG, "Error: " + error.getMessage());
                        }
                    }) {
                        @Override
                        public Map<String, String> getHeaders() throws AuthFailureError {
                            Map<String, String> headers = super.getHeaders();

                            if (headers == null
                                    || headers.equals(Collections.emptyMap())) {
                                headers = new HashMap<String, String>();
                            }

                            headers.put("user_id", Integer.toString(BaseActivity.userId));
                            headers.put("access_token", BaseActivity.accessToken);

                            return headers;
                        }
                    };

                    AppController.getInstance().addToRequestQueue(jsonObjectRequest, tag);

                } else if (commentText.getText().length() > 200) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewPostActivity.this);
                    alertDialogBuilder.setMessage("You cannot simply skoot with more than 250! For that you would have login through Facebook.");
                    alertDialogBuilder.setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                } else {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ViewPostActivity.this);
                    alertDialogBuilder.setMessage("You cannot simply skoot with no content!");
                    alertDialogBuilder.setPositiveButton("Ok!", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {

                        }
                    });
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }
            }
        });
    }

    public void getCommentsForPostId(Post post, int userId) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", Integer.toString(userId));
        params.put("post_id", Integer.toString(mPost.getId()));

        String url = BaseActivity.substituteString(getResources().getString(R.string.skoot_single), params);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                final String SKOOTS = "skoot";
                final String SKOOT_ID = "id";
                final String SKOOT_POST = "content";
                final String SKOOT_COMMENTS = "comments";
                final String SKOOT_CREATED_AT = "created_at";
                final String SKOOT_UPVOTES = "upvotes";
                final String SKOOT_DOWNVOTES = "downvotes";
                final String SKOOT_IF_USER_VOTED = "if_user_voted";
                final String SKOOT_USER_VOTE = "user_vote";
                final String SKOOT_USER_COMMENT = "user_comment";

                try {
                    JSONArray jsonArray = response.getJSONArray(SKOOT_COMMENTS);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonComment = jsonArray.getJSONObject(i);
                        int id = jsonComment.getInt(SKOOT_ID);
                        String comment = jsonComment.getString(SKOOT_POST);
                        int upvotes = jsonComment.getInt(SKOOT_UPVOTES);
                        int downvotes = jsonComment.getInt(SKOOT_DOWNVOTES);
                        boolean if_user_voted = jsonComment.getBoolean(SKOOT_IF_USER_VOTED);
                        boolean user_vote = false;
                        if (if_user_voted) {
                            user_vote = jsonComment.getBoolean(SKOOT_USER_VOTE);
                        }
                        boolean user_skoot = jsonComment.getBoolean(SKOOT_USER_COMMENT);
                        String timestamp = jsonComment.getString(SKOOT_CREATED_AT);

                        Comment commentObject = new Comment(id, mPost.getId(), comment, upvotes, downvotes, if_user_voted, user_vote, user_skoot, timestamp);
                        mCommentsList.add(commentObject);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(LOG_TAG, "Error processing JSON data");
                }
                mCommentsAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d(LOG_TAG, "Error: " + error.getMessage());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();

                if (headers == null
                        || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<String, String>();
                }

                headers.put("user_id", Integer.toString(BaseActivity.userId));
                headers.put("access_token", BaseActivity.accessToken);

                return headers;
            }
        };

        AppController.getInstance().addToRequestQueue(jsonObjectRequest, "view_comments");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_view_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        } else if (id == R.id.action_share) {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, mPost.getContent() + ". Join me on Skooter!");
            startActivity(Intent.createChooser(share, "Share post with friends"));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void dismissView(View view) {
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.flag_view);
        linearLayout.setVisibility(View.GONE);
        linearLayout = (LinearLayout) findViewById(R.id.delete_view);
        linearLayout.setVisibility(View.GONE);
    }

    public void flagContent(View view) {
        String type = (String) view.getTag();
        TextView typeId = (TextView) findViewById(R.id.type_id);
        TextView typeView = (TextView) findViewById(R.id.type);
        LinearLayout flagView = (LinearLayout) findViewById(R.id.flag_view);

        if (typeView.getText().equals("post")) {
            //Flag a post
            String url = BaseActivity.substituteString(getResources().getString(R.string.flag_skoot), new HashMap<String, String>());

            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", Integer.toString(BaseActivity.userId));
            params.put("post_id", typeId.getText().toString());
            params.put("type_id", type);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(LOG_TAG, response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(LOG_TAG, "Error: " + error.getMessage());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = super.getHeaders();

                    if (headers == null
                            || headers.equals(Collections.emptyMap())) {
                        headers = new HashMap<String, String>();
                    }

                    headers.put("user_id", Integer.toString(BaseActivity.userId));
                    headers.put("access_token", BaseActivity.accessToken);

                    return headers;
                }
            };

            AppController.getInstance().addToRequestQueue(jsonObjectRequest, "flag_skoot");
        } else if (typeView.getText().equals("comment")) {
            //Flag a comment
            String url = BaseActivity.substituteString(getResources().getString(R.string.flag_comment), new HashMap<String, String>());

            Map<String, String> params = new HashMap<String, String>();
            params.put("user_id", Integer.toString(BaseActivity.userId));
            params.put("comment_id", typeId.getText().toString());
            params.put("type_id", type);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.d(LOG_TAG, response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(LOG_TAG, "Error: " + error.getMessage());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = super.getHeaders();

                    if (headers == null
                            || headers.equals(Collections.emptyMap())) {
                        headers = new HashMap<String, String>();
                    }

                    headers.put("user_id", Integer.toString(BaseActivity.userId));
                    headers.put("access_token", BaseActivity.accessToken);

                    return headers;
                }
            };

            AppController.getInstance().addToRequestQueue(jsonObjectRequest, "flag_comment");
        }

        flagView.setVisibility(View.GONE);

    }

    public void acceptDelete(View view) {
        TextView typeId = (TextView) findViewById(R.id.type_id);
        TextView typeView = (TextView) findViewById(R.id.type);
        LinearLayout deleteView = (LinearLayout) findViewById(R.id.delete_view);

        if (typeView.getText().equals("post")) {
            //Delete a post
            Map<String, String> params = new HashMap<String, String>();
            params.put("skoot_id", typeId.getText().toString());
            String url = BaseActivity.substituteString(getResources().getString(R.string.skoot_delete), params);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v(LOG_TAG, "Delete Post" + response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(LOG_TAG, "Delete Post Error: " + error.getMessage());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = super.getHeaders();

                    if (headers == null
                            || headers.equals(Collections.emptyMap())) {
                        headers = new HashMap<String, String>();
                    }

                    headers.put("user_id", Integer.toString(BaseActivity.userId));
                    headers.put("access_token", BaseActivity.accessToken);

                    return headers;
                }
            };

            AppController.getInstance().addToRequestQueue(jsonObjectRequest, "flag_content");
        } else if (typeView.getText().equals("comment")) {
            //Delete a comment
            Map<String, String> params = new HashMap<String, String>();
            params.put("comment_id", typeId.getText().toString());
            String url = BaseActivity.substituteString(getResources().getString(R.string.comment_delete), params);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.DELETE, url, null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.v(LOG_TAG, "Delete Comment" + response.toString());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    VolleyLog.d(LOG_TAG, "Delete Comment Error: " + error.getMessage());
                }
            }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = super.getHeaders();

                    if (headers == null
                            || headers.equals(Collections.emptyMap())) {
                        headers = new HashMap<String, String>();
                    }

                    headers.put("user_id", Integer.toString(BaseActivity.userId));
                    headers.put("access_token", BaseActivity.accessToken);

                    return headers;
                }
            };

            AppController.getInstance().addToRequestQueue(jsonObjectRequest, "flag_content");
        }
        deleteView.setVisibility(View.GONE);
    }
}
