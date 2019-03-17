package seedu.address.logic.parser;

import static seedu.address.commons.core.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.math.BigDecimal;

import seedu.address.logic.commands.SetConsultationFeeCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/**
 * Parses input argument and returns a SetConsultationFeeCommand
 */
public class SetConsultationFeeCommandParser {

    private static final String REGEX = "^\\$?\\d+(\\.\\d{2})?$";
    /**
     * Parses the given {@code String} of arguments in the context of the SetConsultationFeeCommand
     * and returns a SetConsultationFeeCommand object for execution
     * @throws ParseException if the user input does not conform the expected format
     */
    public SetConsultationFeeCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        final String[] tokens = trimmedArgs.split("\\s+");
        // check that there is only 1 argument
        if (tokens.length != 1) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, SetConsultationFeeCommand.MESSAGE_USAGE));
        }
        if (!tokens[0].matches(REGEX)) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, SetConsultationFeeCommand.MESSAGE_USAGE));
        }
        String token = tokens[0];
        if (token.charAt(0) == '$') {
            token = token.substring(1);
        }
        String cents = "00";
        int len = token.length();
        if (len > 3 && token.charAt(len - 3) == '.') {
            cents = token.substring(len - 2);
            token = token.substring(0, len - 3);
        }
        String dollars = token;
        final String fee = dollars + "." + cents;
        return new SetConsultationFeeCommand(new BigDecimal(fee));
    }
}
