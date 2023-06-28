package com.example.parkingspotskopjebackend.service;

import com.example.parkingspotskopjebackend.model.Waiting;
import com.example.parkingspotskopjebackend.service.callbacks.PersonReleasedSpotCallback;
import com.google.firebase.database.*;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FirebaseService {
    private final DatabaseReference databaseReference;

    public FirebaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    public void getFirstPersonWaitingForParking(String parkingId, PersonReleasedSpotCallback callback) {
        databaseReference.child("waitings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Waiting> waitings = new ArrayList<>();
                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    Waiting waiting = childSnapshot.getValue(Waiting.class);
                    waiting.setId(childSnapshot.getKey());
                    waitings.add(waiting);
                }
                Waiting waiting = waitings.stream()
                        .filter(x -> x.getParkingId().equals(parkingId))
                        .sorted(Comparator.comparing(x -> x.getTimestamp()))
                        .limit(1)
                        .findFirst()
                        .orElse(null);
                String userId=waiting.getUserId();
                // Remove the waiting object from the database
                deleteWaitingFromFirebase(waiting);

                databaseReference.child("users").child(userId.replace(".", ",")).child("deviceToken").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String deviceToken = dataSnapshot.getValue(String.class);
                        callback.onFirstPersonFound(userId, deviceToken);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.err.println("Failed to retrieve Device Token: " + databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                callback.onCancelled(databaseError.getMessage());
            }
        });
    }
    private void deleteWaitingFromFirebase(Waiting waiting){
        DatabaseReference nodeRef = databaseReference.child("waitings").child(waiting.getId());
        nodeRef.removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.out.println("Error occurred while removing the node: " + databaseError.getMessage());
                } else {
                    System.out.println("Node removed successfully.");
                }
            }
        });
    }

    public void sendPersonReleasedSpotNotification(String recipientToken, String senderId, String parkingName, String parkingId, String senderName) {
        try{
            // Create a Notification instance
            Notification notification = Notification.builder()
                    .setTitle(parkingName+" has a free spot!")
                    .setBody(senderName+" has just left the parking. Tap to thank them!")
                    .build();

            // Create a Message instance
            Message message = Message.builder()
                    .setNotification(notification)
                    .putData("type","OTHER")
                    .putData("senderId", senderId)
                    .putData("senderName", senderName)
                    .putData("parkingName",parkingName)
                    .putData("parkingId",parkingId)
                    .setToken(recipientToken)
                    .build();

            // Send the message
            String response = FirebaseMessaging.getInstance().send(message);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void sayThanksNotification(String senderUserId, String senderUserName, String receiverUserId, String receiverUserName){
        databaseReference.child("users").child(receiverUserId.replace(".", ",")).child("deviceToken").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String deviceToken = dataSnapshot.getValue(String.class);
                sendThankYouNotification(deviceToken,senderUserId,senderUserName, receiverUserId,receiverUserName);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });
    }
    public void sendThankYouNotification(String recipientToken, String senderId, String senderName, String receiverUserId, String receiverUserName){
        try{
            // Create a Notification instance
            Notification notification = Notification.builder()
                    .setTitle(senderName+" has thanked you!")
                    .setBody(senderName+" thanks you for informing them regarding the spot you left!")
                    .build();

            // Create a Message instance
            Message message = Message.builder()
                    .setNotification(notification)
                    .putData("type","THANKS")
                    .putData("senderId", senderId)
                    .setToken(recipientToken)
                    .build();

            // Send the message
            String response = FirebaseMessaging.getInstance().send(message);
        }
        catch (Exception e){
            System.out.println(e.getMessage());
        }
    }


}
