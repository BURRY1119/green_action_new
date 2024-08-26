package com.example.green_action.Community;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.green_action.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final List<Comment> commentList;
    private final Context context;
    private final String postId;

    public CommentAdapter(List<Comment> commentList, Context context, String postId) {
        this.commentList = commentList;
        this.context = context;
        this.postId = postId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = commentList.get(position);

        // UID의 앞 7자리만 가져오기
        String displayId = comment.getUserId().substring(0, Math.min(comment.getUserId().length(), 7));

        // 사용자 이름과 UID의 앞 7자리를 함께 표시
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(comment.getUserId());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String userName = snapshot.child("name").getValue(String.class);
                if (userName != null) {
                    holder.userInfo.setText(userName + " (" + displayId + ")");
                } else {
                    holder.userInfo.setText("Unknown User (" + displayId + ")");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.userInfo.setText("Unknown User (" + displayId + ")");
            }
        });

        holder.content.setText(comment.getCommentText());
        holder.timestamp.setText(convertTimestampToDate(comment.getTimestamp()));

        // 댓글 아이템 클릭 시 로그인된 사용자와 댓글 작성자의 UID가 같은지 확인
        holder.itemView.setOnClickListener(v -> {
            FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
            if (currentUser != null && currentUser.getUid().equals(comment.getUserId())) {
                Intent intent = new Intent(context, EditCommentActivity.class);
                intent.putExtra("commentId", comment.getCommentId());
                intent.putExtra("postId", postId);
                intent.putExtra("commentText", comment.getCommentText());
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "수정 권한이 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userInfo, content, timestamp;

        ViewHolder(View itemView) {
            super(itemView);
            userInfo = itemView.findViewById(R.id.comment_user_info);
            content = itemView.findViewById(R.id.comment_content);
            timestamp = itemView.findViewById(R.id.comment_timestamp);
        }
    }

    private String convertTimestampToDate(long timestamp) {
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }
}
