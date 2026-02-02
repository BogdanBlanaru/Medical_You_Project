package com.zega.medical_you_be.util;

import com.zega.medical_you_be.model.entity.HealthAlert;
import com.zega.medical_you_be.model.entity.HealthReading;
import com.zega.medical_you_be.model.enums.AlertSeverity;
import com.zega.medical_you_be.model.enums.ReadingType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class HealthRangeValidator {

    // Normal ranges for different reading types
    // Based on standard medical guidelines

    public List<HealthAlert> validateReading(HealthReading reading) {
        List<HealthAlert> alerts = new ArrayList<>();

        switch (reading.getReadingType()) {
            case BLOOD_GLUCOSE -> validateBloodGlucose(reading, alerts);
            case BLOOD_PRESSURE -> validateBloodPressure(reading, alerts);
            case HEART_RATE -> validateHeartRate(reading, alerts);
            case TEMPERATURE -> validateTemperature(reading, alerts);
            case OXYGEN_SATURATION -> validateOxygenSaturation(reading, alerts);
            case WEIGHT -> {} // Weight alerts depend on BMI calculation
            case BMI -> validateBMI(reading, alerts);
            case CHOLESTEROL -> validateCholesterol(reading, alerts);
            case STEPS -> {} // Steps don't generate alerts
        }

        return alerts;
    }

    private void validateBloodGlucose(HealthReading reading, List<HealthAlert> alerts) {
        BigDecimal value = reading.getValue();
        double glucose = value.doubleValue();

        // Normal fasting: 70-100 mg/dL
        // Prediabetes: 100-125 mg/dL
        // Diabetes: >125 mg/dL
        // Hypoglycemia: <70 mg/dL
        // Severe hypoglycemia: <55 mg/dL

        if (glucose < 55) {
            alerts.add(createAlert(reading, AlertSeverity.CRITICAL,
                    "Severe hypoglycemia detected! Blood glucose is critically low at " + glucose + " mg/dL. Seek immediate medical attention."));
        } else if (glucose < 70) {
            alerts.add(createAlert(reading, AlertSeverity.WARNING,
                    "Low blood glucose (hypoglycemia) at " + glucose + " mg/dL. Consider consuming fast-acting carbohydrates."));
        } else if (glucose > 180) {
            alerts.add(createAlert(reading, AlertSeverity.CRITICAL,
                    "Very high blood glucose at " + glucose + " mg/dL. Contact your healthcare provider."));
        } else if (glucose > 125) {
            alerts.add(createAlert(reading, AlertSeverity.WARNING,
                    "Elevated blood glucose at " + glucose + " mg/dL. This may indicate diabetes."));
        } else if (glucose > 100) {
            alerts.add(createAlert(reading, AlertSeverity.INFO,
                    "Slightly elevated blood glucose at " + glucose + " mg/dL. This may indicate prediabetes."));
        }
    }

    private void validateBloodPressure(HealthReading reading, List<HealthAlert> alerts) {
        double systolic = reading.getValue().doubleValue();
        double diastolic = reading.getSecondaryValue() != null ? reading.getSecondaryValue().doubleValue() : 0;

        // Normal: <120/<80 mmHg
        // Elevated: 120-129/<80 mmHg
        // High Stage 1: 130-139/80-89 mmHg
        // High Stage 2: >=140/>=90 mmHg
        // Crisis: >180/>120 mmHg
        // Low: <90/<60 mmHg

        if (systolic > 180 || diastolic > 120) {
            alerts.add(createAlert(reading, AlertSeverity.CRITICAL,
                    "Hypertensive crisis! Blood pressure is dangerously high at " + (int)systolic + "/" + (int)diastolic + " mmHg. Seek immediate medical care."));
        } else if (systolic >= 140 || diastolic >= 90) {
            alerts.add(createAlert(reading, AlertSeverity.WARNING,
                    "High blood pressure (Stage 2 Hypertension) at " + (int)systolic + "/" + (int)diastolic + " mmHg. Consult your doctor."));
        } else if (systolic >= 130 || diastolic >= 80) {
            alerts.add(createAlert(reading, AlertSeverity.INFO,
                    "Elevated blood pressure (Stage 1 Hypertension) at " + (int)systolic + "/" + (int)diastolic + " mmHg. Monitor regularly."));
        } else if (systolic < 90 || diastolic < 60) {
            alerts.add(createAlert(reading, AlertSeverity.WARNING,
                    "Low blood pressure (Hypotension) at " + (int)systolic + "/" + (int)diastolic + " mmHg. You may feel dizzy or faint."));
        }
    }

    private void validateHeartRate(HealthReading reading, List<HealthAlert> alerts) {
        double heartRate = reading.getValue().doubleValue();

        // Normal resting: 60-100 bpm
        // Bradycardia: <60 bpm (can be normal for athletes)
        // Tachycardia: >100 bpm
        // Dangerous: <40 or >150 bpm

        if (heartRate < 40) {
            alerts.add(createAlert(reading, AlertSeverity.CRITICAL,
                    "Critically low heart rate at " + (int)heartRate + " bpm. Seek immediate medical attention."));
        } else if (heartRate > 150) {
            alerts.add(createAlert(reading, AlertSeverity.CRITICAL,
                    "Critically high heart rate at " + (int)heartRate + " bpm. Seek immediate medical attention."));
        } else if (heartRate < 50) {
            alerts.add(createAlert(reading, AlertSeverity.WARNING,
                    "Low heart rate (bradycardia) at " + (int)heartRate + " bpm. Consult your doctor if you experience symptoms."));
        } else if (heartRate > 120) {
            alerts.add(createAlert(reading, AlertSeverity.WARNING,
                    "High heart rate (tachycardia) at " + (int)heartRate + " bpm. Rest and monitor."));
        } else if (heartRate > 100) {
            alerts.add(createAlert(reading, AlertSeverity.INFO,
                    "Slightly elevated heart rate at " + (int)heartRate + " bpm."));
        }
    }

    private void validateTemperature(HealthReading reading, List<HealthAlert> alerts) {
        double temp = reading.getValue().doubleValue();

        // Normal: 36.1-37.2 °C (97-99 °F)
        // Low-grade fever: 37.3-38.0 °C
        // Fever: 38.1-39.4 °C
        // High fever: >39.4 °C
        // Hypothermia: <35 °C

        if (temp < 35) {
            alerts.add(createAlert(reading, AlertSeverity.CRITICAL,
                    "Hypothermia detected! Body temperature is dangerously low at " + temp + " °C. Seek immediate medical attention."));
        } else if (temp > 40) {
            alerts.add(createAlert(reading, AlertSeverity.CRITICAL,
                    "Very high fever at " + temp + " °C. Seek immediate medical attention."));
        } else if (temp > 39.4) {
            alerts.add(createAlert(reading, AlertSeverity.WARNING,
                    "High fever at " + temp + " °C. Consider seeking medical attention."));
        } else if (temp > 38) {
            alerts.add(createAlert(reading, AlertSeverity.WARNING,
                    "Fever detected at " + temp + " °C. Rest and stay hydrated."));
        } else if (temp > 37.2) {
            alerts.add(createAlert(reading, AlertSeverity.INFO,
                    "Slightly elevated temperature at " + temp + " °C."));
        }
    }

    private void validateOxygenSaturation(HealthReading reading, List<HealthAlert> alerts) {
        double spo2 = reading.getValue().doubleValue();

        // Normal: 95-100%
        // Mild hypoxemia: 91-94%
        // Moderate hypoxemia: 86-90%
        // Severe hypoxemia: <86%

        if (spo2 < 86) {
            alerts.add(createAlert(reading, AlertSeverity.CRITICAL,
                    "Severe hypoxemia! Oxygen saturation is critically low at " + spo2 + "%. Seek immediate medical attention."));
        } else if (spo2 < 90) {
            alerts.add(createAlert(reading, AlertSeverity.CRITICAL,
                    "Moderate hypoxemia! Oxygen saturation at " + spo2 + "%. Contact your doctor immediately."));
        } else if (spo2 < 94) {
            alerts.add(createAlert(reading, AlertSeverity.WARNING,
                    "Low oxygen saturation at " + spo2 + "%. Monitor closely."));
        }
    }

    private void validateBMI(HealthReading reading, List<HealthAlert> alerts) {
        double bmi = reading.getValue().doubleValue();

        // Underweight: <18.5
        // Normal: 18.5-24.9
        // Overweight: 25-29.9
        // Obese: >=30

        if (bmi < 16) {
            alerts.add(createAlert(reading, AlertSeverity.CRITICAL,
                    "Severely underweight with BMI of " + bmi + ". Consult a healthcare provider."));
        } else if (bmi < 18.5) {
            alerts.add(createAlert(reading, AlertSeverity.WARNING,
                    "Underweight with BMI of " + bmi + ". Consider consulting a nutritionist."));
        } else if (bmi >= 35) {
            alerts.add(createAlert(reading, AlertSeverity.WARNING,
                    "Severe obesity with BMI of " + bmi + ". Increased health risks. Consult your doctor."));
        } else if (bmi >= 30) {
            alerts.add(createAlert(reading, AlertSeverity.INFO,
                    "Obese with BMI of " + bmi + ". Consider lifestyle changes."));
        } else if (bmi >= 25) {
            alerts.add(createAlert(reading, AlertSeverity.INFO,
                    "Overweight with BMI of " + bmi + ". Monitor your weight."));
        }
    }

    private void validateCholesterol(HealthReading reading, List<HealthAlert> alerts) {
        double cholesterol = reading.getValue().doubleValue();

        // Desirable: <200 mg/dL
        // Borderline high: 200-239 mg/dL
        // High: >=240 mg/dL

        if (cholesterol >= 240) {
            alerts.add(createAlert(reading, AlertSeverity.WARNING,
                    "High cholesterol at " + cholesterol + " mg/dL. Consult your doctor about treatment options."));
        } else if (cholesterol >= 200) {
            alerts.add(createAlert(reading, AlertSeverity.INFO,
                    "Borderline high cholesterol at " + cholesterol + " mg/dL. Consider dietary changes."));
        }
    }

    private HealthAlert createAlert(HealthReading reading, AlertSeverity severity, String message) {
        return HealthAlert.builder()
                .healthReading(reading)
                .severity(severity)
                .message(message)
                .isAcknowledged(false)
                .build();
    }

    // Get the unit for a reading type
    public String getUnitForType(ReadingType type) {
        return switch (type) {
            case BLOOD_GLUCOSE -> "mg/dL";
            case BLOOD_PRESSURE -> "mmHg";
            case WEIGHT -> "kg";
            case HEART_RATE -> "bpm";
            case TEMPERATURE -> "°C";
            case OXYGEN_SATURATION -> "%";
            case BMI -> "";
            case CHOLESTEROL -> "mg/dL";
            case STEPS -> "steps";
        };
    }
}
