package com.abhigarg.notepadapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.abhigarg.notepadapp.model.Adapter;
import com.abhigarg.notepadapp.model.Note;
import com.abhigarg.notepadapp.note.AddNote;
import com.abhigarg.notepadapp.note.EditNote;
import com.abhigarg.notepadapp.note.NoteDetails;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class note_list extends AppCompatActivity {
    RecyclerView noteLists;
    FirebaseFirestore fStore;
    FirebaseAuth fAuth;
    FirebaseUser user;
    FirestoreRecyclerAdapter<Note, note_list.NoteViewHolder> noteAdapter;
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        fab=findViewById(R.id.noteListFab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), AddNote.class));
            }
        });

        noteLists = findViewById(R.id.notelist);
        fStore= FirebaseFirestore.getInstance();
        fAuth= FirebaseAuth.getInstance();
        user=fAuth.getCurrentUser();


        Query query= fStore.collection("notes").document(user.getUid()).collection("myNotes").orderBy("title", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> allNotes=new FirestoreRecyclerOptions.Builder<Note>().setQuery(query,Note.class).build();

        noteAdapter=new FirestoreRecyclerAdapter<Note, NoteViewHolder>(allNotes) {
            @Override
            protected void onBindViewHolder(@NonNull NoteViewHolder noteViewHolder, int i, @NonNull final Note note) {
                 noteViewHolder.noteContent.setText(note.getContent());
                 noteViewHolder.noteTitle.setText(note.getTitle());
                 noteViewHolder.noteContent.setTextColor(note.getTextColor());
                 noteViewHolder.noteContent.setBackgroundColor(note.getBackgroundColor());
                 final String docId=noteAdapter.getSnapshots().getSnapshot(i).getId();
                 noteViewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i=new Intent(v.getContext(), NoteDetails.class);
                        i.putExtra("title",note.getTitle());
                        i.putExtra("content",note.getContent());
                        i.putExtra("noteId",docId);
                        i.putExtra("textColor",note.getTextColor());
                        i.putExtra("backgroundColor",note.getBackgroundColor());
                        v.getContext().startActivity(i);
                    }
                });

                ImageView menuIcon=noteViewHolder.view.findViewById(R.id.menuIcon);
                menuIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        PopupMenu menu=new PopupMenu(v.getContext(),v);
                        menu.getMenu().add("Edit").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                Intent i= new Intent(v.getContext(), EditNote.class);
                                i.putExtra("title",note.getTitle());
                                i.putExtra("content",note.getContent());
                                i.putExtra("noteId",docId);
                                i.putExtra("textColor",note.getTextColor());
                                i.putExtra("backgroundColor",note.getBackgroundColor());
                                startActivity(i);
                                return false;
                            }
                        });
                        menu.getMenu().add("Delete").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                DocumentReference docRef=fStore.collection("notes").document(user.getUid()).collection("myNotes").document(docId);
                                docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        //note deleted
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(),"Error in Deleting Notes",Toast.LENGTH_SHORT).show();
                                    }
                                });
                                return false;
                            }
                        });
                        menu.show();
                    }
                });

            }

            @NonNull
            @Override
            public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.note_view_layout,parent,false);
                return new NoteViewHolder(view);
            }
        };

        noteLists.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteLists.setAdapter(noteAdapter);

    }
    public class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView noteTitle,noteContent;
        View view;
        //String textColor;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            noteTitle=itemView.findViewById(R.id.titles);
            noteContent=itemView.findViewById(R.id.content);
            view=itemView;
        }

    }

    @Override
    protected void onStart(){
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop(){
        super.onStop();
        if(noteAdapter!=null) {
            noteAdapter.stopListening();
        }
    }
}
