package de.pauleff.jnbt.formats.snbt;

import de.pauleff.jnbt.api.ICompoundTag;
import de.pauleff.jnbt.api.IListTag;
import de.pauleff.jnbt.api.ITag;
import de.pauleff.jnbt.api.NBTFactory;
import de.pauleff.jnbt.util.NBTTags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class SNBTBooleanStringTest
{
    @ParameterizedTest
    @ValueSource(strings = {"\"true\"", "'true'", "true", "false"})
    void tokenizerRetainsQuoteInformation(String input) throws SNBTException
    {
        SNBTTokenizer tokenizer = new SNBTTokenizer("  " + input);
        SNBTTokenizer.Token token = tokenizer.nextToken();
        assertEquals(SNBTTokenizer.TokenType.STRING, token.type());
        assertEquals(input.startsWith("\"") || input.startsWith("'"), token.quoted());
        assertEquals(input.contains("true") ? "true" : "false", token.value());
        assertEquals(2, token.position());
        assertEquals(SNBTTokenizer.TokenType.EOF, tokenizer.nextToken().type());
    }

    @Test
    void threeArgumentTokenConstructorCreatesUnquotedToken()
    {
        SNBTTokenizer.Token token = new SNBTTokenizer.Token(SNBTTokenizer.TokenType.STRING, "true", 3);
        assertEquals(SNBTTokenizer.TokenType.STRING, token.type());
        assertEquals("true", token.value());
        assertEquals(3, token.position());
        assertFalse(token.quoted());
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"true\"", "'true'", "\"false\"", "'false'",
            "\"\\u0074rue\"", "'\\u0066alse'"})
    void quotedBooleansRemainStrings(String input) throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT(input);
        assertEquals(NBTTags.Tag_String.getId(), tag.getId());
        assertEquals(input.contains("rue") ? "true" : "false", tag.getData());
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void unquotedBooleansRemainBytes(String input) throws SNBTException
    {
        ITag<?> tag = NBTFactory.parseFromSNBT(input);
        assertEquals(NBTTags.Tag_Byte.getId(), tag.getId());
        assertEquals((byte) (input.equals("true") ? 1 : 0), tag.getData());
    }

    @ParameterizedTest
    @ValueSource(strings = {"{true:'false',false:\"true\"}",
            "{'true':\"false\",'false':'true'}", "{\"true\":'false',\"false\":\"true\"}"})
    void booleanLookingKeysAndValuesRemainStrings(String input) throws SNBTException
    {
        ICompoundTag compound = NBTFactory.parseCompoundFromSNBT(input);
        assertEquals(2, compound.getData().size());
        assertEquals("false", compound.getString("true"));
        assertEquals("true", compound.getString("false"));
    }

    @Test
    void nestedValuesUseTheirOwnQuoteInformation() throws SNBTException
    {
        ICompoundTag compound = NBTFactory.parseCompoundFromSNBT(
                "{nested:{text:'true',flag:false},text:\"false\",flag:true}");
        ICompoundTag nested = compound.getCompound("nested");
        assertEquals("true", nested.getString("text"));
        assertEquals((byte) 0, nested.getByte("flag"));
        assertEquals("false", compound.getString("text"));
        assertEquals((byte) 1, compound.getByte("flag"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"['true',\"false\"]", "[\"true\",'false',ordinary]"})
    void quotedBooleansBelongInStringLists(String input) throws SNBTException
    {
        IListTag list = NBTFactory.parseListFromSNBT(input);
        assertEquals(NBTTags.Tag_String.getId(), list.getListTypeID());
        assertEquals("true", list.getData().get(0).getData());
        assertEquals("false", list.getData().get(1).getData());
    }

    @Test
    void unquotedBooleanListRemainsByteTyped() throws SNBTException
    {
        IListTag list = NBTFactory.parseListFromSNBT("[true,false]");
        assertEquals(NBTTags.Tag_Byte.getId(), list.getListTypeID());
        assertEquals((byte) 1, list.getData().get(0).getData());
        assertEquals((byte) 0, list.getData().get(1).getData());
    }

    @ParameterizedTest
    @ValueSource(strings = {"['true',false]", "[true,'false']", "[B;'true']", "[B;\"false\"]"})
    void quotedBooleansCannotBeUsedAsBytes(String input)
    {
        assertThrows(SNBTException.class, () -> NBTFactory.parseFromSNBT(input));
    }

    @ParameterizedTest
    @ValueSource(strings = {"true", "false"})
    void stringRoundTripsPreserveTypeAndValue(String value) throws SNBTException
    {
        ITag<?> parsed = NBTFactory.parseFromSNBT(
                NBTFactory.toSNBT(NBTFactory.createString("s", value)));
        assertEquals(NBTTags.Tag_String.getId(), parsed.getId());
        assertEquals(value, parsed.getData());

        ICompoundTag original = NBTFactory.createCompound()
                .addString(value, value).addByte("flag", (byte) 1);
        ICompoundTag compound = NBTFactory.parseCompoundFromSNBT(NBTFactory.toSNBT(original));
        assertEquals(value, compound.getString(value));
        assertEquals((byte) 1, compound.getByte("flag"));

        IListTag originalList = NBTFactory.createList("", NBTTags.Tag_String.getId())
                .addString("", value).addString("", "ordinary");
        IListTag list = NBTFactory.parseListFromSNBT(NBTFactory.toSNBT(originalList));
        assertEquals(NBTTags.Tag_String.getId(), list.getListTypeID());
        assertEquals(value, list.getData().getFirst().getData());
        assertEquals("ordinary", list.getData().getLast().getData());
    }
}
