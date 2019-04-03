package seedu.address.logic.commands;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandFailure;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_REMINDER;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_REMINDER;
import static seedu.address.testutil.TypicalReminders.getTypicalRemindersQuickDocs;

import org.junit.Test;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.CommandHistory;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.QuickDocs;
import seedu.address.model.UserPrefs;
import seedu.address.model.reminder.Reminder;


/**
 * Contains integration tests (interaction with the Model, UndoCommand and RedoCommand) and unit tests for
 * {@code DeleteCommand}.
 */
public class DeleteRemCommandTest {

    private QuickDocs quickDocs = getTypicalRemindersQuickDocs();
    private Model model = new ModelManager(quickDocs, new UserPrefs());
    private CommandHistory commandHistory = new CommandHistory();

    @Test
    public void execute_validIndexFilteredList_success() {
        Reminder reminderToDelete = model.getFilteredReminderList().get(INDEX_FIRST_REMINDER.getZeroBased());
        DeleteRemCommand deleteRemCommand = new DeleteRemCommand(INDEX_FIRST_REMINDER);

        String expectedMessage = String.format(DeleteRemCommand.MESSAGE_DELETE_REMINDER_SUCCESS, reminderToDelete);

        ModelManager expectedModel = new ModelManager(getTypicalRemindersQuickDocs(), new UserPrefs());
        expectedModel.deleteReminder(reminderToDelete);

        assertCommandSuccess(deleteRemCommand, model, commandHistory, expectedMessage, expectedModel);
    }

    @Test
    public void execute_invalidIndexFilteredList_throwsCommandException() {
        Index outOfBoundIndex = Index.fromOneBased(model.getFilteredReminderList().size() + 1);
        DeleteRemCommand deleteRemCommand = new DeleteRemCommand(outOfBoundIndex);

        assertCommandFailure(deleteRemCommand, model, commandHistory, DeleteRemCommand.MESSAGE_INVALID_REMINDER_INDEX);
    }

    @Test
    public void equals() {
        DeleteRemCommand deleteFirstCommand = new DeleteRemCommand(INDEX_FIRST_REMINDER);
        DeleteRemCommand deleteSecondCommand = new DeleteRemCommand(INDEX_SECOND_REMINDER);

        // same object -> returns true
        assertTrue(deleteFirstCommand.equals(deleteFirstCommand));

        // same values -> returns true
        DeleteRemCommand deleteFirstCommandCopy = new DeleteRemCommand(INDEX_FIRST_REMINDER);
        assertTrue(deleteFirstCommand.equals(deleteFirstCommandCopy));

        // different types -> returns false
        assertFalse(deleteFirstCommand.equals(1));

        // null -> returns false
        assertFalse(deleteFirstCommand.equals(null));

        // different person -> returns false
        assertFalse(deleteFirstCommand.equals(deleteSecondCommand));
    }
}
