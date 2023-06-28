package com.example.parkingspotskopjebackend.web;

import com.example.parkingspotskopjebackend.service.FirebaseService;
import com.example.parkingspotskopjebackend.service.callbacks.PersonReleasedSpotCallback;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class NotificationsRestController {

    private final FirebaseService firebaseService;

    public NotificationsRestController(FirebaseService firebaseService) {
        this.firebaseService = firebaseService;
    }

    @GetMapping("/sendReleasedSpotNotification")
    public void sendReleasedSpotNotification(@RequestParam  String senderUserId,
                                             @RequestParam  String senderUserName,
                                             @RequestParam  String parkingId,
                                             @RequestParam  String parkingName){
        firebaseService.getFirstPersonWaitingForParking(parkingId, new PersonReleasedSpotCallback() {
            @Override
            public void onFirstPersonFound(String userId, String deviceToken) {
                if(userId!=null && deviceToken!=null){
                    //send notification to user
                    firebaseService.sendPersonReleasedSpotNotification(deviceToken,senderUserId,parkingName,parkingId,senderUserName);
                }
            }

            @Override
            public void onCancelled(String errorMessage) {

            }
        });
    }
    @GetMapping("/sayThanksNotification")
    public void sayThanksNotification(@RequestParam String senderUserId,
                                      @RequestParam String senderUserName,
                                      @RequestParam String receiverUserId,
                                      @RequestParam String receiverUserName){
        firebaseService.sayThanksNotification(senderUserId,senderUserName,receiverUserId,receiverUserName);
    }

    @GetMapping("/sendCustomNotification")
    public void sendCustomNotification(@RequestParam String deviceToken,
                                       @RequestParam String senderUserName,
                                       @RequestParam String senderUserId,
                                       @RequestParam String parkingName,
                                       @RequestParam String parkingId){
        firebaseService.sendPersonReleasedSpotNotification(deviceToken,senderUserId,parkingName, parkingId,senderUserName);
    }
}
