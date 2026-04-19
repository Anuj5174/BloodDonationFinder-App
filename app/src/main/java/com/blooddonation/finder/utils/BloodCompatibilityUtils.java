package com.blooddonation.finder.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WHO-standard ABO/Rh blood transfusion compatibility rules.
 * canDonateTo(donor) → list of blood types the donor can donate to.
 * compatibleDonors(recipient) → list of blood types that can donate to recipient.
 */
public class BloodCompatibilityUtils {

    // Who can donate TO each blood type (recipient → list of valid donor types)
    private static final Map<String, List<String>> COMPATIBLE_DONORS = new HashMap<>();

    // Who a donor can donate TO
    private static final Map<String, List<String>> CAN_DONATE_TO = new HashMap<>();

    static {
        COMPATIBLE_DONORS.put("A+",  Arrays.asList("A+", "A-", "O+", "O-"));
        COMPATIBLE_DONORS.put("A-",  Arrays.asList("A-", "O-"));
        COMPATIBLE_DONORS.put("B+",  Arrays.asList("B+", "B-", "O+", "O-"));
        COMPATIBLE_DONORS.put("B-",  Arrays.asList("B-", "O-"));
        COMPATIBLE_DONORS.put("AB+", Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"));
        COMPATIBLE_DONORS.put("AB-", Arrays.asList("A-", "B-", "O-", "AB-"));
        COMPATIBLE_DONORS.put("O+",  Arrays.asList("O+", "O-"));
        COMPATIBLE_DONORS.put("O-",  Arrays.asList("O-"));

        CAN_DONATE_TO.put("A+",  Arrays.asList("A+", "AB+"));
        CAN_DONATE_TO.put("A-",  Arrays.asList("A+", "A-", "AB+", "AB-"));
        CAN_DONATE_TO.put("B+",  Arrays.asList("B+", "AB+"));
        CAN_DONATE_TO.put("B-",  Arrays.asList("B+", "B-", "AB+", "AB-"));
        CAN_DONATE_TO.put("AB+", Arrays.asList("AB+"));
        CAN_DONATE_TO.put("AB-", Arrays.asList("AB+", "AB-"));
        CAN_DONATE_TO.put("O+",  Arrays.asList("A+", "B+", "O+", "AB+"));
        CAN_DONATE_TO.put("O-",  Arrays.asList("A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"));
    }

    /**
     * Returns all blood types that are compatible donors for the given recipient.
     */
    public static List<String> compatibleDonors(String recipientBloodGroup) {
        List<String> result = COMPATIBLE_DONORS.get(recipientBloodGroup);
        return result != null ? result : Arrays.asList(recipientBloodGroup);
    }

    /**
     * Returns all blood types this donor can donate to.
     */
    public static List<String> canDonateTo(String donorBloodGroup) {
        List<String> result = CAN_DONATE_TO.get(donorBloodGroup);
        return result != null ? result : Arrays.asList(donorBloodGroup);
    }

    /**
     * Returns true if donorBloodGroup can donate to recipientBloodGroup.
     */
    public static boolean isCompatible(String donorBloodGroup, String recipientBloodGroup) {
        List<String> donors = COMPATIBLE_DONORS.get(recipientBloodGroup);
        return donors != null && donors.contains(donorBloodGroup);
    }

    /**
     * Human-readable label summarising compatibility info.
     */
    public static String getCompatibilityLabel(String donorBloodGroup) {
        List<String> donateTo = canDonateTo(donorBloodGroup);
        return donorBloodGroup + " → can donate to: " + String.join(", ", donateTo);
    }
}
