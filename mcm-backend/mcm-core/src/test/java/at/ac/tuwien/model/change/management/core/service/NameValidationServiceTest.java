package at.ac.tuwien.model.change.management.core.service;

import at.ac.tuwien.model.change.management.core.exception.InvalidNameException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class NameValidationServiceTest {

    private final NameValidationService nameValidationService = new NameValidationServiceImpl();

    @Test
    public void testValidateRepositoryName_validName_shouldNotThrowException() {
        String name = "validName";
        Assertions.assertThatCode(() -> nameValidationService.validateRepositoryName(name))
                .doesNotThrowAnyException();
    }

    @Test
    public void testValidateRepositoryName_blankName_shouldThrowInvalidNameException() {
        String name = " ";
        Assertions.assertThatThrownBy(() -> nameValidationService.validateRepositoryName(name))
                .isInstanceOf(InvalidNameException.class)
                .hasMessage("Repository name must not be blank");
    }

    @Test
    public void testValidateRepositoryName_nameWith127Characters_shouldNotThrowException() {
        String name = "a".repeat(127);
        Assertions.assertThatCode(() -> nameValidationService.validateRepositoryName(name))
                .doesNotThrowAnyException();
    }

    @Test
    public void testValidateRepositoryName_nameWith128Characters_shouldThrowInvalidNameException() {
        String name = "a".repeat(128);
        Assertions.assertThatThrownBy(() -> nameValidationService.validateRepositoryName(name))
                .isInstanceOf(InvalidNameException.class)
                .hasMessage("Repository name cannot be longer than 127 characters");
    }

    @Test
    public void testValidateRepositoryName_nameWithForwardSlash_shouldThrowInvalidNameException() {
        String name = "name/with/slash";
        Assertions.assertThatThrownBy(() -> nameValidationService.validateRepositoryName(name))
                .isInstanceOf(InvalidNameException.class)
                .hasMessage("Repository name cannot contain path separators such as '/' or '\\'");
    }

    @Test
    public void testValidateRepositoryName_nameWithBackslash_shouldThrowInvalidNameException() {
        String name = "name\\with\\backslash";
        Assertions.assertThatThrownBy(() -> nameValidationService.validateRepositoryName(name))
                .isInstanceOf(InvalidNameException.class)
                .hasMessage("Repository name cannot contain path separators such as '/' or '\\'");
    }

    @Test
    public void testValidateRepositoryName_nameWithInvalidCharacter_shouldThrowInvalidNameException() {
        String name = "invalid!name";
        Assertions.assertThatThrownBy(() -> nameValidationService.validateRepositoryName(name))
                .isInstanceOf(InvalidNameException.class)
                .hasMessage("Repository name contains invalid characters");
    }

    @Test
    public void testValidateRepositoryName_nameStartingWithDot_shouldThrowInvalidNameException() {
        String name = ".startingWithDot";
        Assertions.assertThatThrownBy(() -> nameValidationService.validateRepositoryName(name))
                .isInstanceOf(InvalidNameException.class)
                .hasMessage("Repository name must not start or end with hyphens or periods");
    }

    @Test
    public void testValidateRepositoryName_nameEndingWithDot_shouldThrowInvalidNameException() {
        String name = "endingWithDot.";
        Assertions.assertThatThrownBy(() -> nameValidationService.validateRepositoryName(name))
                .isInstanceOf(InvalidNameException.class)
                .hasMessage("Repository name must not start or end with hyphens or periods");
    }

    @Test
    public void testValidateRepositoryName_nameStartingWithHyphen_shouldThrowInvalidNameException() {
        String name = "-startingWithHyphen";
        Assertions.assertThatThrownBy(() -> nameValidationService.validateRepositoryName(name))
                .isInstanceOf(InvalidNameException.class)
                .hasMessage("Repository name must not start or end with hyphens or periods");
    }

    @Test
    public void testValidateRepositoryName_nameEndingWithHyphen_shouldThrowInvalidNameException() {
        String name = "endingWithHyphen-";
        Assertions.assertThatThrownBy(() -> nameValidationService.validateRepositoryName(name))
                .isInstanceOf(InvalidNameException.class)
                .hasMessage("Repository name must not start or end with hyphens or periods");
    }

    @Test
    public void testValidateRepositoryName_reservedNameCON_shouldThrowInvalidNameException() {
        String name = "CON";
        Assertions.assertThatThrownBy(() -> nameValidationService.validateRepositoryName(name))
                .isInstanceOf(InvalidNameException.class)
                .hasMessage("Repository name contains keyword possibly reserved by the operating system");
    }

    @Test
    public void testValidateRepositoryName_reservedNameCOM1Lowercase_shouldThrowInvalidNameException() {
        String name = "com1";
        Assertions.assertThatThrownBy(() -> nameValidationService.validateRepositoryName(name))
                .isInstanceOf(InvalidNameException.class)
                .hasMessage("Repository name contains keyword possibly reserved by the operating system");
    }

    @Test
    public void testValidateRepositoryName_reservedNameLPT2_shouldThrowInvalidNameException() {
        String name = "LPT2";
        Assertions.assertThatThrownBy(() -> nameValidationService.validateRepositoryName(name))
                .isInstanceOf(InvalidNameException.class)
                .hasMessage("Repository name contains keyword possibly reserved by the operating system");
    }


    @Test
    public void testEncodeVersionName_space_shouldEncodeSpace() {
        String name = " name with space ";
        String result = nameValidationService.encodeVersionName(name, false);
        Assertions.assertThat(result).isEqualTo("%20name%20with%20space%20");
    }

    @Test
    public void testEncodeVersionName_backslash_shouldEncodeBackslash() {
        String name = "\\name\\with\\backslash\\";
        String result = nameValidationService.encodeVersionName(name, false);
        Assertions.assertThat(result).isEqualTo("%5Cname%5Cwith%5Cbackslash%5C");
    }

    @Test
    public void testEncodeVersionName_questionMark_shouldEncodeQuestionMark() {
        String name = "?name?with?question?";
        String result = nameValidationService.encodeVersionName(name, false);
        Assertions.assertThat(result).isEqualTo("%3Fname%3Fwith%3Fquestion%3F");
    }

    @Test
    public void testEncodeVersionName_tilde_shouldEncodeTilde() {
        String name = "~name~with~tilde~";
        String result = nameValidationService.encodeVersionName(name, false);
        Assertions.assertThat(result).isEqualTo("%7Ename%7Ewith%7Etilde%7E");
    }

    @Test
    public void testEncodeVersionName_caret_shouldEncodeCaret() {
        String name = "^name^with^caret^";
        String result = nameValidationService.encodeVersionName(name, false);
        Assertions.assertThat(result).isEqualTo("%5Ename%5Ewith%5Ecaret%5E");
    }

    @Test
    public void testEncodeVersionName_colon_shouldEncodeColon() {
        String name = ":name:with:colon:";
        String result = nameValidationService.encodeVersionName(name, false);
        Assertions.assertThat(result).isEqualTo("%3Aname%3Awith%3Acolon%3A");
    }

    @Test
    public void testEncodeVersionName_asterisk_shouldEncodeAsterisk() {
        String name = "*name*with*asterisk*";
        String result = nameValidationService.encodeVersionName(name, false);
        Assertions.assertThat(result).isEqualTo("%2Aname%2Awith%2Aasterisk%2A");
    }

    @Test
    public void testEncodeVersionName_leftBracket_shouldEncodeLeftBracket() {
        String name = "[name[with[bracket[";
        String result = nameValidationService.encodeVersionName(name, false);
        Assertions.assertThat(result).isEqualTo("%5Bname%5Bwith%5Bbracket%5B");
    }

    @Test
    public void testEncodeVersionName_atSymbol_shouldEncodeAtSymbol() {
        String name = "@name@with@at@";
        String result = nameValidationService.encodeVersionName(name, false);
        Assertions.assertThat(result).isEqualTo("%40name%40with%40at%40");
    }

    @Test
    public void testEncodeVersionName_forwardSlash_shouldEncodeForwardSlash() {
        String name = "/name/with/slash/";
        String result = nameValidationService.encodeVersionName(name, false);
        Assertions.assertThat(result).isEqualTo("%2Fname%2Fwith%2Fslash%2F");
    }

    @Test
    public void testEncodeVersionName_combined_shouldEncodeAll() {
        String name = "name with space\\question?tilde~caret^colon:asterisk*leftBracket[q";
        String result = nameValidationService.encodeVersionName(name, false);
        Assertions.assertThat(result).isEqualTo("name%20with%20space%5Cquestion%3Ftilde%7Ecaret%5Ecolon%3Aasterisk%2AleftBracket%5Bq");
    }

    @Test
    public void testEncodeVersionName_combined_shouldEncodeAllExceptPeriods() {
        String name = "name.with.periods\\and?special~chars";
        String result = nameValidationService.encodeVersionName(name, false);
        Assertions.assertThat(result).isEqualTo("name.with.periods%5Cand%3Fspecial%7Echars");
    }

    @Test
    public void testEncodeVersionName_sanitize_shouldRemoveConsecutiveDashes() {
        String name = "name--with--dashes";
        String result = nameValidationService.encodeVersionName(name, true);
        Assertions.assertThat(result).isEqualTo("name-with-dashes");
    }

    @Test
    public void testEncodeVersionName_sanitize_shouldRemoveConsecutiveDot() {
        String name = "name..with..dots";
        String result = nameValidationService.encodeVersionName(name, true);
        Assertions.assertThat(result).isEqualTo("name.with.dots");
    }

    @Test
    public void testEncodeVersionName_sanitize_shouldRemoveLeadingOrTrailingDashes() {
        String name = "-name-with-dots-and-dashes-";
        String result = nameValidationService.encodeVersionName(name, true);
        Assertions.assertThat(result).isEqualTo("name-with-dots-and-dashes");
    }

    @Test
    public void testEncodeVersionName_sanitize_shouldRemoveLeadingOrTrailingDots() {
        String name = ".name.with.dots.and.dashes.";
        String result = nameValidationService.encodeVersionName(name, true);
        Assertions.assertThat(result).isEqualTo("name.with.dots.and.dashes");
    }

    @Test
    public void testEncodeVersionName_sanitize_shouldRemoveLeadingOrTrailingDashesAndDots() {
        String name = "-.name.with.dots.and.dashes.-";
        String result = nameValidationService.encodeVersionName(name, true);
        Assertions.assertThat(result).isEqualTo("name.with.dots.and.dashes");
    }

    @Test
    public void testEncodeVersionName_semanticVersioning_shouldNotChangeVersion() {
        Assertions.assertThat(nameValidationService.encodeVersionName("v1.0.0", true))
                .isEqualTo("v1.0.0");
        Assertions.assertThat(nameValidationService.encodeVersionName("v0.0.0", true))
                .isEqualTo("v0.0.0");
        Assertions.assertThat(nameValidationService.encodeVersionName("v.0.9.9", true))
                .isEqualTo("v.0.9.9");
        Assertions.assertThat(nameValidationService.encodeVersionName("v9.9.9", true))
                .isEqualTo("v9.9.9");
        Assertions.assertThat(nameValidationService.encodeVersionName("v100000.9.1234567890", true))
                .isEqualTo("v100000.9.1234567890");
    }

    @Test
    public void testEncodeVersionName_gitCommitHash_shouldNotChangeHash() {
        Assertions.assertThat(nameValidationService.encodeVersionName("4d8f8fcbc63d6e4f8e9c64d9a93897cd6d6b4f9c", true))
                .isEqualTo("4d8f8fcbc63d6e4f8e9c64d9a93897cd6d6b4f9c");
        Assertions.assertThat(nameValidationService.encodeVersionName("abc123def4567890abcdef1234567890abcdef12", true))
                .isEqualTo("abc123def4567890abcdef1234567890abcdef12");
        Assertions.assertThat(nameValidationService.encodeVersionName("ffffffffffffffffffffffffffffffffffffffff", true))
                .isEqualTo("ffffffffffffffffffffffffffffffffffffffff");
        Assertions.assertThat(nameValidationService.encodeVersionName("0000000000000000000000000000000000000000", true))
                .isEqualTo("0000000000000000000000000000000000000000");
        Assertions.assertThat(nameValidationService.encodeVersionName("AaBbCcDdEeFf1234567890AaBbCcDdEeFf12345678", true))
                .isEqualTo("AaBbCcDdEeFf1234567890AaBbCcDdEeFf12345678");
    }

    @Test
    public void testDecodeVersionName_space_shouldDecodeSpace() {
        String name = "%20name%20with%20space%20";
        String result = nameValidationService.decodeVersionName(name);
        Assertions.assertThat(result).isEqualTo(" name with space ");
    }

    @Test
    public void testDecodeVersionName_backslash_shouldDecodeBackslash() {
        String name = "%5Cname%5Cwith%5Cbackslash%5C";
        String result = nameValidationService.decodeVersionName(name);
        Assertions.assertThat(result).isEqualTo("\\name\\with\\backslash\\");
    }

    @Test
    public void testDecodeVersionName_questionMark_shouldDecodeQuestionMark() {
        String name = "%3Fname%3Fwith%3Fquestion%3F";
        String result = nameValidationService.decodeVersionName(name);
        Assertions.assertThat(result).isEqualTo("?name?with?question?");
    }

    @Test
    public void testDecodeVersionName_tilde_shouldDecodeTilde() {
        String name = "%7Ename%7Ewith%7Etilde%7E";
        String result = nameValidationService.decodeVersionName(name);
        Assertions.assertThat(result).isEqualTo("~name~with~tilde~");
    }

    @Test
    public void testDecodeVersionName_caret_shouldDecodeCaret() {
        String name = "%5Ename%5Ewith%5Ecaret%5E";
        String result = nameValidationService.decodeVersionName(name);
        Assertions.assertThat(result).isEqualTo("^name^with^caret^");
    }

    @Test
    public void testDecodeVersionName_colon_shouldDecodeColon() {
        String name = "%3Aname%3Awith%3Acolon%3A";
        String result = nameValidationService.decodeVersionName(name);
        Assertions.assertThat(result).isEqualTo(":name:with:colon:");
    }

    @Test
    public void testDecodeVersionName_asterisk_shouldDecodeAsterisk() {
        String name = "%2Aname%2Awith%2Aasterisk%2A";
        String result = nameValidationService.decodeVersionName(name);
        Assertions.assertThat(result).isEqualTo("*name*with*asterisk*");
    }

    @Test
    public void testDecodeVersionName_leftBracket_shouldDecodeLeftBracket() {
        String name = "%5Bname%5Bwith%5Bbracket%5B";
        String result = nameValidationService.decodeVersionName(name);
        Assertions.assertThat(result).isEqualTo("[name[with[bracket[");
    }

    @Test
    public void testDecodeVersionName_atSymbol_shouldDecodeAtSymbol() {
        String name = "%40name%40with%40at%40";
        String result = nameValidationService.decodeVersionName(name);
        Assertions.assertThat(result).isEqualTo("@name@with@at@");
    }

    @Test
    public void testDecodeVersionName_forwardSlash_shouldDecodeForwardSlash() {
        String name = "%2Fname%2Fwith%2Fslash%2F";
        String result = nameValidationService.decodeVersionName(name);
        Assertions.assertThat(result).isEqualTo("/name/with/slash/");
    }

    @Test
    public void testDecodeVersionName_combined_shouldDecodeAll() {
        String name = "name%20with%20space%5Cquestion%3Ftilde%7Ecaret%5Ecolon%3Aasterisk%2AleftBracket%5Bq";
        String result = nameValidationService.decodeVersionName(name);
        Assertions.assertThat(result).isEqualTo("name with space\\question?tilde~caret^colon:asterisk*leftBracket[q");
    }

    @Test
    public void testDecodeVersionName_combined_shouldDecodeAllExceptPeriods() {
        String name = "name.with.periods%5Cand%3Fspecial%7Echars";
        String result = nameValidationService.decodeVersionName(name);
        Assertions.assertThat(result).isEqualTo("name.with.periods\\and?special~chars");
    }

    @Test
    public void testDecodeVersionName_semanticVersioning_shouldNotChangeVersion() {
        Assertions.assertThat(nameValidationService.decodeVersionName("v1.0.0"))
                .isEqualTo("v1.0.0");
        Assertions.assertThat(nameValidationService.decodeVersionName("v0.0.0"))
                .isEqualTo("v0.0.0");
        Assertions.assertThat(nameValidationService.decodeVersionName("v.0.9.9"))
                .isEqualTo("v.0.9.9");
        Assertions.assertThat(nameValidationService.decodeVersionName("v9.9.9"))
                .isEqualTo("v9.9.9");
        Assertions.assertThat(nameValidationService.decodeVersionName("v100000.9.1234567890"))
                .isEqualTo("v100000.9.1234567890");
    }

    @Test
    public void testDecodeVersionName_gitCommitHash_shouldNotChangeHash() {
        Assertions.assertThat(nameValidationService.decodeVersionName("4d8f8fcbc63d6e4f8e9c64d9a93897cd6d6b4f9c"))
                .isEqualTo("4d8f8fcbc63d6e4f8e9c64d9a93897cd6d6b4f9c");
        Assertions.assertThat(nameValidationService.decodeVersionName("abc123def4567890abcdef1234567890abcdef12"))
                .isEqualTo("abc123def4567890abcdef1234567890abcdef12");
        Assertions.assertThat(nameValidationService.decodeVersionName("ffffffffffffffffffffffffffffffffffffffff"))
                .isEqualTo("ffffffffffffffffffffffffffffffffffffffff");
        Assertions.assertThat(nameValidationService.decodeVersionName("0000000000000000000000000000000000000000"))
                .isEqualTo("0000000000000000000000000000000000000000");
        Assertions.assertThat(nameValidationService.decodeVersionName("AaBbCcDdEeFf1234567890AaBbCcDdEeFf12345678"))
                .isEqualTo("AaBbCcDdEeFf1234567890AaBbCcDdEeFf12345678");
        Assertions.assertThat(nameValidationService.decodeVersionName("4d8f8fc"))
                .isEqualTo("4d8f8fc");
    }
}
