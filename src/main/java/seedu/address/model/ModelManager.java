package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.appointment.Appointment;
import seedu.address.model.appointment.AppointmentManager;
import seedu.address.model.consultation.Consultation;
import seedu.address.model.consultation.ConsultationManager;
import seedu.address.model.consultation.Diagnosis;
import seedu.address.model.consultation.Prescription;
import seedu.address.model.medicine.Directory;
import seedu.address.model.medicine.Medicine;
import seedu.address.model.medicine.MedicineManager;
import seedu.address.model.patient.Nric;
import seedu.address.model.patient.Patient;
import seedu.address.model.patient.PatientManager;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.record.MedicinePurchaseRecord;
import seedu.address.model.record.Record;
import seedu.address.model.record.Statistics;
import seedu.address.model.record.StatisticsManager;
import seedu.address.model.reminder.Reminder;
import seedu.address.model.reminder.ReminderManager;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleAppUtil;
import seedu.address.model.util.SamplePatientsUtil;
import seedu.address.model.util.SampleRemUtil;


/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final VersionedAddressBook versionedAddressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SimpleObjectProperty<Person> selectedPerson = new SimpleObjectProperty<>();
    // to handle QuickDocs operations
    private final QuickDocs quickDocs;
    private final FilteredList<Reminder> filteredReminders;
    private final SimpleObjectProperty<Reminder> selectedReminder = new SimpleObjectProperty<>();
    private final MedicineManager medicineManager;
    private final PatientManager patientManager;
    private final ConsultationManager consultationManager;
    private final AppointmentManager appointmentManager;
    private final ReminderManager reminderManager;
    private final StatisticsManager statisticsManager;


    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        super();
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        versionedAddressBook = new VersionedAddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(versionedAddressBook.getPersonList());
        filteredPersons.addListener(this::ensureSelectedPersonIsValid);
        this.medicineManager = new MedicineManager();
        this.patientManager = new PatientManager(addressBook.getPatients());
        this.consultationManager = new ConsultationManager();
        this.appointmentManager = new AppointmentManager();
        this.reminderManager = new ReminderManager();
        this.statisticsManager = new StatisticsManager();

        quickDocs = new QuickDocs();
        filteredReminders = new FilteredList<>(FXCollections.observableArrayList(reminderManager.getReminderList()));


        iniQuickDocs();
    }

    /**
     * Initializes a ModelManager with the given addressBook quickdocs and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, QuickDocs quickDocs, ReadOnlyUserPrefs userPrefs) {
        super();
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        this.quickDocs = quickDocs;

        versionedAddressBook = new VersionedAddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        filteredPersons = new FilteredList<>(versionedAddressBook.getPersonList());
        filteredPersons.addListener(this::ensureSelectedPersonIsValid);
        this.medicineManager = quickDocs.getMedicineManager();
        //this.patientManager = new PatientManager(addressBook.getPatients());
        this.patientManager = quickDocs.getPatientManager();
        this.consultationManager = quickDocs.getConsultationManager();
        this.appointmentManager = quickDocs.getAppointmentManager();
        this.reminderManager = quickDocs.getReminderManager();
        this.statisticsManager = quickDocs.getStatisticsManager();

        filteredReminders = new FilteredList<>(FXCollections.observableArrayList(reminderManager.getReminderList()));

        iniQuickDocs();
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    /**
     * Initialise quickdocs with sample patient data
     */
    public void iniQuickDocs() {
        Patient[] samplePatients = SamplePatientsUtil.getSamplePatients();
        //for (Patient patient : samplePatients) {
        //    addPatient(patient);
        //}
        Appointment[] sampleAppointments = SampleAppUtil.getSampleAppointments(samplePatients);
        //for (Appointment app : sampleAppointments) {
        //    addApp(app);
        //}
        Reminder[] sampleReminders = SampleRemUtil.getSampleReminders();
        //for (Reminder rem : sampleReminders) {
        //    addRem(rem);
        //}
    }

    /**
     * Initialise quickdocs with sample patient data for testing purposes
     */
    public void initQuickDocsSampleData() {
        Patient[] samplePatients = SamplePatientsUtil.getSamplePatients();
        for (Patient patient : samplePatients) {
            addPatient(patient);
        }
        Appointment[] sampleAppointments = SampleAppUtil.getSampleAppointments(samplePatients);
        for (Appointment app : sampleAppointments) {
            addApp(app);
        }
        Reminder[] sampleReminders = SampleRemUtil.getSampleReminders();
        for (Reminder rem : sampleReminders) {
            addRem(rem);
        }
    }

    public QuickDocs getQuickDocs() {
        return quickDocs;
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public void setUserPrefs(ReadOnlyUserPrefs userPrefs) {
        requireNonNull(userPrefs);
        this.userPrefs.resetData(userPrefs);
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public Path getAddressBookFilePath() {
        return userPrefs.getAddressBookFilePath();
    }

    @Override
    public void setAddressBookFilePath(Path addressBookFilePath) {
        requireNonNull(addressBookFilePath);
        userPrefs.setAddressBookFilePath(addressBookFilePath);
    }

    @Override
    public void deleteTag(Tag tag) {
        versionedAddressBook.removeTag(tag);
    }

    //=========== MedicineManager ============================================================================
    @Override
    public void addMedicine(String medicineName, String[] path, BigDecimal price) {
        Medicine medicine = medicineManager.addMedicine(medicineName, path, price);
        reminderForMedicine(medicine);
        quickDocs.indicateModification(true);
    }

    @Override
    public void addMedicine(String medicineName, int quantity, String[] path, BigDecimal price) {
        Medicine medicine = medicineManager.addMedicine(medicineName, quantity, path, price);
        reminderForMedicine(medicine);
        quickDocs.indicateModification(true);
    }

    @Override
    public void addExistingMedicineToDirectory(Medicine medicine, String[] path) {
        medicineManager.addExistingMedicineToDirectory(medicine, path);
        quickDocs.indicateModification(true);
    }

    @Override
    public void addDirectory(String directoryName, String[] path) {
        medicineManager.addDirectory(directoryName, path);
        quickDocs.indicateModification(true);
    }

    @Override
    public Optional<Medicine> findMedicine(String medicineName) {
        return medicineManager.findMedicine(medicineName);
    }

    @Override
    public Optional<Medicine> findMedicine(String[] path) {
        return medicineManager.findMedicine(path);
    }

    @Override
    public void purchaseMedicine(String[] path, int quantity, BigDecimal cost) {
        Medicine medicine = medicineManager.purchaseMedicine(path, quantity);
        reminderForMedicine(medicine);
        addRecord(new MedicinePurchaseRecord(medicine, quantity, cost), Clock.systemDefaultZone());
        quickDocs.indicateModification(true);
    }

    @Override
    public void purchaseMedicine(String medicineName, int quantity, BigDecimal cost) {
        Medicine medicine = medicineManager.purchaseMedicine(medicineName, quantity);
        reminderForMedicine(medicine);
        addRecord(new MedicinePurchaseRecord(medicine, quantity, cost), Clock.systemDefaultZone());
        quickDocs.indicateModification(true);
    }

    @Override
    public Optional<Directory> findDirectory(String[] path) {
        return medicineManager.findDirectory(path);
    }

    @Override
    public void setThreshold(Medicine medicine, int threshold) {
        medicine.setThreshold(threshold);
        reminderForMedicine(medicine);
        quickDocs.indicateModification(true);
    }

    @Override
    public void setThreshold(Directory directory, int threshold) {
        directory.setThreshold(threshold);
        for (Medicine medicine : directory.getListOfMedicine()) {
            setThreshold(medicine, threshold);
        }
        for (Directory subDirectory : directory.getListOfDirectory()) {
            setThreshold(subDirectory, threshold);
        }
        quickDocs.indicateModification(true);
    }

    @Override
    public void setPrice(Medicine medicine, BigDecimal price) {
        medicine.setPrice(price);
        quickDocs.indicateModification(true);
    }
    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        versionedAddressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return versionedAddressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return versionedAddressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        versionedAddressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        versionedAddressBook.addPerson(person);
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        versionedAddressBook.setPerson(target, editedPerson);
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return filteredPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
    }

    @Override
    public void updateFilteredReminderList(Predicate<Reminder> predicate) {
        requireNonNull(predicate);
        filteredReminders.setPredicate(predicate);
    }

    @Override
    public ObservableList<Reminder> getFilteredReminderList() {
        return filteredReminders;
    }

    @Override
    public ObservableList<Reminder> getFilteredReminderList(LocalDate date) {
        return reminderManager.getFilteredReminderList(date);
    }

    //=========== Undo/Redo =================================================================================

    @Override
    public void commitAddressBook() {
        versionedAddressBook.commit();
    }

    //=========== Selected person ===========================================================================

    @Override
    public ReadOnlyProperty<Person> selectedPersonProperty() {
        return selectedPerson;
    }

    @Override
    public ReadOnlyProperty<Reminder> selectedReminderProperty() {
        return selectedReminder;
    }

    @Override
    public Person getSelectedPerson() {
        return selectedPerson.getValue();
    }

    @Override
    public void setSelectedPerson(Person person) {
        if (person != null && !filteredPersons.contains(person)) {
            throw new PersonNotFoundException();
        }
        selectedPerson.setValue(person);
    }

    @Override
    public void setSelectedReminder(Reminder reminder) {
        if (reminder != null && !reminderManager.getReminderList().contains(reminder)) {
            throw new PersonNotFoundException();
        }
        selectedReminder.setValue(reminder);
    }

    /**
     * Ensures {@code selectedPerson} is a valid person in {@code filteredPersons}.
     */
    private void ensureSelectedPersonIsValid(ListChangeListener.Change<? extends Person> change) {
        while (change.next()) {
            if (selectedPerson.getValue() == null) {
                // null is always a valid selected person, so we do not need to check that it is valid anymore.
                return;
            }

            boolean wasSelectedPersonReplaced = change.wasReplaced() && change.getAddedSize() == change.getRemovedSize()
                    && change.getRemoved().contains(selectedPerson.getValue());
            if (wasSelectedPersonReplaced) {
                // Update selectedPerson to its new value.
                int index = change.getRemoved().indexOf(selectedPerson.getValue());
                selectedPerson.setValue(change.getAddedSubList().get(index));
                continue;
            }

            boolean wasSelectedPersonRemoved = change.getRemoved().stream()
                    .anyMatch(removedPerson -> selectedPerson.getValue().isSamePerson(removedPerson));
            if (wasSelectedPersonRemoved) {
                // Select the person that came before it in the list,
                // or clear the selection if there is no such person.
                selectedPerson.setValue(change.getFrom() > 0 ? change.getList().get(change.getFrom() - 1) : null);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return versionedAddressBook.equals(other.versionedAddressBook)
                //&& medicineManager.equals(other.medicineManager)
                //&& reminderManager.equals(other.reminderManager)
                //&& patientManager.equals(other.patientManager)
                //&& consultationManager.equals(other.consultationManager)
                //&& appointmentManager.equals(other.appointmentManager)
                && userPrefs.equals(other.userPrefs)
                && filteredPersons.equals(other.filteredPersons)
                && Objects.equals(selectedPerson.get(), other.selectedPerson.get());
    }

    //==========Patient module============================================================================

    // for adding
    public boolean duplicatePatient(Patient patient) {
        return this.patientManager.isDuplicatePatient(patient);
    }

    /**
     * Add a patient to the quickdocs
     */
    public void addPatient(Patient patient) {
        this.patientManager.addPatient(patient);
        quickDocs.indicateModification(true);
    }

    // for editing
    public boolean isPatientListEmpty() {
        return this.patientManager.isPatientListEmpty();
    }

    public boolean checkValidIndex(int index) {
        return this.patientManager.checkValidIndex(index);
    }

    public Patient getPatientAtIndex(int index) {
        return this.patientManager.getPatientAtIndex(index);
    }

    public boolean checkDuplicatePatientAfterEdit(int index, Patient editedPatient) {
        return this.patientManager.checkDuplicatePatientAfterEdit(index, editedPatient);
    }

    /**
     * Replace the patient at index with the edited version
     */
    public void replacePatient(int index, Patient editedPatient) {
        this.patientManager.replacePatient(index, editedPatient);
        quickDocs.indicateModification(true);
    }

    // for listing
    public String findPatientsByName(String searchSequence) {
        return this.patientManager.findPatientsByName(searchSequence);
    }

    public String listFiftyPatients() {
        return this.patientManager.listFiftyPatients();
    }

    public String findPatientsByNric(String searchSequence) {
        return this.patientManager.findPatientsByNric(searchSequence);
    }

    public String findPatientsByTag(Tag tag) {
        return this.patientManager.findPatientsByTag(tag);
    }

    public Patient getPatientByNric(String nric) {
        return this.patientManager.getPatientByNric(nric);
    }

    public Optional<Patient> getPatientByNric(Nric nric) {
        return this.patientManager.getPatientByNric(nric);
    }

    public int getIndexByNric(Nric nric) {
        return this.patientManager.getIndexByNric(nric); }

    /**
     * Delete patient from patient records
     *
     * @param nric of the patient to be deleted
     */
    public void deletePatientByNric(String nric) {
        this.patientManager.deletePatientByNric(nric);
        quickDocs.indicateModification(true);
    }

    //==========Consultation module============================================================================

    public void createConsultation(Patient patient) {
        this.consultationManager.createConsultation(patient);
    }

    public void diagnosePatient(Diagnosis diagnosis) {
        this.consultationManager.diagnosePatient(diagnosis);
    }

    public boolean checkConsultation() {
        return this.consultationManager.checkConsultation();
    }

    public void prescribeMedicine(ArrayList<Prescription> prescriptions) {
        this.consultationManager.prescribeMedicine(prescriptions);
    }

    /**
     * end the current consultation session, no further edits can be made
     */
    public void endConsultation() {
        this.consultationManager.endConsultation();
        quickDocs.indicateModification(true);
    }

    public Consultation getCurrentConsultation() {
        return this.consultationManager.getCurrentConsultation();
    }

    public ArrayList<Consultation> getConsultationList() {
        return this.consultationManager.getConsultationList();
    }

    public ArrayList<Consultation> listConsultation(String value) {
        return this.consultationManager.listConsultation(value);
    }

    public Consultation listConsultation(int index) {
        return this.consultationManager.listConsultation(index);
    }

    //==========Appointment module===========================================================================
    public boolean hasTimeConflicts(Appointment app) {
        return appointmentManager.hasTimeConflicts(app);
    }

    /**
     * Adds an {@code Appointment} and its {@code Reminder} to QuickDocs
     *
     * @param app the {@code Appointment} to add
     */
    public void addApp(Appointment app) {
        appointmentManager.addAppointment(app);
        Reminder remToAdd = createRemFromApp(app);
        addRem((remToAdd));
        quickDocs.indicateModification(true);
    }

    public String listApp(LocalDate start, LocalDate end) {
        return appointmentManager.listAppointments(start, end);
    }

    public String listApp(Patient patient) {
        return appointmentManager.listAppointments(patient);
    }

    public String freeApp(LocalDate start, LocalDate end) {
        return appointmentManager.listFreeSlots(start, end);
    }

    public Optional<Appointment> getAppointment(LocalDate date, LocalTime start) {
        return appointmentManager.getAppointment(date, start);
    }

    /**
     * Deletes an {@code Appointment} from QuickDocs
     *
     * @param appointment the {@code Appointment} to delete
     */
    public void deleteAppointment(Appointment appointment) {
        Optional<Reminder> reminder = reminderManager.getReminder(appointment);
        if (reminder.isPresent()) {
            reminderManager.delete(reminder.get());
        }
        appointmentManager.delete(appointment);
        quickDocs.indicateModification(true);
    }

    //==========Reminder module==============================================================================
    public boolean duplicateRem(Reminder rem) {
        return reminderManager.hasDuplicateReminder(rem);
    }

    /**
     * Adds a {@code Reminder} to QuickDocs
     *
     * @param rem the {@code Reminder} to add
     */
    public void addRem(Reminder rem) {
        reminderManager.addReminder(rem);
        quickDocs.indicateModification(true);
    }

    public String listRem() {
        return reminderManager.list();
    }

    public Optional<Reminder> getReminder(Appointment appointment) {
        return reminderManager.getReminder(appointment);
    }

    private Reminder createRemFromApp(Appointment app) {
        return new Reminder(app.createTitle(), app.getComment(), app.getDate(), app.getStart(), app.getEnd());
    }

    /**
     * Deletes a {@code Reminder} from QuickDocs
     *
     * @param reminder the {@code Reminder} to be deleted
     */
    public void deleteReminder(Reminder reminder) {
        reminderManager.delete(reminder);
        quickDocs.indicateModification(true);
    }

    /**
     * Add a reminder for a medicine with insufficient amount
     * @param medicine medicine that is below threshold
     */
    public void reminderForMedicine(Medicine medicine) {
        reminderManager.reminderForMedicine(medicine);
        quickDocs.indicateModification(true);
    }

    /**
     * delete outdated medicine amount reminder
     * @param medicine medicine that is no longer in need of reminder
     */
    public void deleteExistingReminderForMedicine(Medicine medicine) {
        if (reminderManager.deleteExistingMedicineReminder(medicine)) {
            quickDocs.indicateModification(true);
        }
    }
    //==========Record module================================================================================
    public Statistics getStatistics(YearMonth from, YearMonth to) {
        return statisticsManager.getStatistics(from, to);
    }

    /**
     * Adds a {@code Record} converted to {@code Statistics} to QuickDocs
     */
    public void addRecord(Record record, Clock clock) {
        statisticsManager.record(record, clock);
        quickDocs.indicateModification(true);
    }

    public void setConsultationFee(BigDecimal fee) {
        statisticsManager.setConsultationFee(fee);
        quickDocs.indicateModification(true);
    }
}
