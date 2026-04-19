package com.blooddonation.finder.models;

public class Donor {
    private String uid;
    private String name;
    private String email;
    private String phone;
    private String bloodGroup;
    private String city;
    private double latitude;
    private double longitude;
    private boolean available;
    private String profileImageUrl;
    private int donationCount;
    private long lastDonationDate;
    private String fcmToken;
    private boolean isAdmin;

    public Donor() {}

    public Donor(String uid, String name, String email, String phone,
                 String bloodGroup, String city, double latitude, double longitude) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bloodGroup = bloodGroup;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
        this.available = true;
        this.donationCount = 0;
        this.isAdmin = false;
    }

    // Getters and Setters
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public int getDonationCount() { return donationCount; }
    public void setDonationCount(int donationCount) { this.donationCount = donationCount; }

    public String getBadgeTitle() {
        if (donationCount >= 10) return "Diamond Saver 💎";
        if (donationCount >= 5) return "Gold Donor 🥇";
        if (donationCount >= 3) return "Silver Lifeline 🥈";
        if (donationCount >= 1) return "Bronze Helper 🥉";
        return "Rookie 🔰";
    }

    public int getLoyaltyPoints() {
        return donationCount * 50;
    }

    public long getLastDonationDate() { return lastDonationDate; }
    public void setLastDonationDate(long lastDonationDate) { this.lastDonationDate = lastDonationDate; }

    public String getFcmToken() { return fcmToken; }
    public void setFcmToken(String fcmToken) { this.fcmToken = fcmToken; }

    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
}
