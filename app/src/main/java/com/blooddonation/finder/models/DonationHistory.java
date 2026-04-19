package com.blooddonation.finder.models;

public class DonationHistory {
    private String historyId;
    private String donorId;
    private String recipientName;
    private String bloodGroup;
    private String location;
    private long donationDate;
    private String requestId;

    public DonationHistory() {}

    public DonationHistory(String donorId, String recipientName, String bloodGroup,
                           String location, long donationDate, String requestId) {
        this.donorId = donorId;
        this.recipientName = recipientName;
        this.bloodGroup = bloodGroup;
        this.location = location;
        this.donationDate = donationDate;
        this.requestId = requestId;
    }

    public String getHistoryId() { return historyId; }
    public void setHistoryId(String historyId) { this.historyId = historyId; }

    public String getDonorId() { return donorId; }
    public void setDonorId(String donorId) { this.donorId = donorId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public long getDonationDate() { return donationDate; }
    public void setDonationDate(long donationDate) { this.donationDate = donationDate; }

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
}
