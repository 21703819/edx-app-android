package org.edx.mobile.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.edx.mobile.R;
import org.edx.mobile.discussion.CommentBody;
import org.edx.mobile.discussion.DiscussionComment;
import org.edx.mobile.discussion.DiscussionCommentPostedEvent;
import org.edx.mobile.discussion.DiscussionTextUtils;
import org.edx.mobile.discussion.DiscussionThread;
import org.edx.mobile.logger.Logger;
import org.edx.mobile.task.CreateCommentTask;

import de.greenrobot.event.EventBus;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectExtra;
import roboguice.inject.InjectView;

public class DiscussionAddResponseFragment extends RoboFragment {

    static public String TAG = DiscussionAddResponseFragment.class.getCanonicalName();

    @InjectExtra(value = Router.EXTRA_DISCUSSION_TOPIC_OBJ, optional = true)
    DiscussionThread discussionTopic;

    protected final Logger logger = new Logger(getClass().getName());

    @InjectView(R.id.etNewComment)
    private EditText editTextNewComment;

    @InjectView(R.id.btnAddComment)
    private Button buttonAddComment;

    @InjectView(R.id.tvTitle)
    private TextView textViewTitle;

    @InjectView(R.id.tvResponse)
    private TextView textViewResponse;

    @InjectView(R.id.tvTimeAuthor)
    private TextView textViewTimeAuthor;
    private CreateCommentTask createCommentTask;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_response, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        textViewTitle.setText(discussionTopic.getTitle());
        textViewResponse.setText(Html.fromHtml(discussionTopic.getRenderedBody()));
        textViewTimeAuthor.setText(DiscussionTextUtils.getAuthorAttributionText(discussionTopic, getResources()));
        buttonAddComment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                createComment();
            }
        });
        buttonAddComment.setEnabled(false);
        editTextNewComment.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                buttonAddComment.setEnabled(s.toString().trim().length() > 0);
            }
        });
    }

    private void createComment() {
        if (createCommentTask != null) {
            createCommentTask.cancel(true);
        }

        final CommentBody commentBody = new CommentBody();
        commentBody.setRawBody(editTextNewComment.getText().toString());
        commentBody.setThreadId(discussionTopic.getIdentifier());
        commentBody.setParentId(null);

        createCommentTask = new CreateCommentTask(getActivity(), commentBody) {
            @Override
            public void onSuccess(@NonNull DiscussionComment thread) {
                logger.debug(thread.toString());
                EventBus.getDefault().post(new DiscussionCommentPostedEvent(thread, null));
                getActivity().finish();
            }

            @Override
            public void onException(Exception ex) {
                logger.error(ex);
            }
        };
        createCommentTask.execute();
    }
}
