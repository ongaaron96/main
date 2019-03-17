package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.QuickDocs;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentManager;
import seedu.address.model.consultation.Consultation;
import seedu.address.model.consultation.ConsultationManager;
import seedu.address.model.patient.Patient;
import seedu.address.model.patient.PatientManager;

/**
 * QuickDocs serializable to json format
 */
public class JsonSerializableQuickDocs {

    private static final String MESSAGE_DUPLICATE_PATIENT = "Patients list contains duplicate patient(s)";
    private static final String MESSAGE_DUPLICATE_APPOINTMENT = "Appointment list contains duplicate appointment(s)";

    private final List<JsonAdaptedPatient> patientList = new ArrayList<>();
    private final List<JsonAdaptedConsultation> consultationList = new ArrayList<>();
    private final List<JsonAdaptedAppointment> appointmentList = new ArrayList<>();

    @JsonCreator
    public JsonSerializableQuickDocs(@JsonProperty("patientList") List<JsonAdaptedPatient> patients) {
        this.patientList.addAll(patients);
    }

    /**
     * Converts a given {@code QuickDocs} into this class for Jackson use.
     */
    public JsonSerializableQuickDocs(QuickDocs source) {
        patientList.addAll(source.getPatientManager().getPatientList()
                .stream().map(JsonAdaptedPatient::new).collect(Collectors.toList()));
        consultationList.addAll(source.getConsultationManager().getConsultationList()
                .stream().map(JsonAdaptedConsultation::new).collect(Collectors.toList()));
        appointmentList.addAll(source.getAppointmentManager().getAppointmentList()
                .stream().map(JsonAdaptedAppointment::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code QuickDocs} object.
     *
     * @throws IllegalValueException    if there were any data constraints violated.
     * @throws IllegalArgumentException if there were any data contraints violated for patient fields
     */
    public QuickDocs toModelType() throws IllegalValueException, IllegalArgumentException {
        QuickDocs quickDocs = new QuickDocs();

        PatientManager patientManager = quickDocs.getPatientManager();
        for (JsonAdaptedPatient jsonAdaptedPatient : patientList) {
            Patient patient = jsonAdaptedPatient.toModelType();

            // handle duplicates
            if (patientManager.duplicatePatient(patient)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PATIENT);
            }
            patientManager.addPatient(patient);
        }

        ConsultationManager consultationManager = quickDocs.getConsultationManager();
        for (JsonAdaptedConsultation jsonAdaptedConsultation : consultationList) {
            Consultation consultation = jsonAdaptedConsultation.toModelType();
            consultationManager.addConsultation(consultation);
        }

        // loop for medicine, appointment, and records
        AppointmentManager appointmentManager = quickDocs.getAppointmentManager();
        for (JsonAdaptedAppointment jsonAdaptedAppointment : appointmentList) {
            Appointment appointment = jsonAdaptedAppointment.toModelType();

            if (appointmentManager.duplicateApp(appointment)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_APPOINTMENT);
            }

            appointmentManager.addAppointment(appointment);
        }
        return quickDocs;
    }

}
