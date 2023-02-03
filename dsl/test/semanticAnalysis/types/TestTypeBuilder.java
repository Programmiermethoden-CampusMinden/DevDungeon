package semanticAnalysis.types;

import static org.junit.Assert.*;

import graph.Graph;
import java.lang.reflect.InvocationTargetException;
import org.junit.Test;
import semanticAnalysis.Scope;
import semanticAnalysis.Symbol;

public class TestTypeBuilder {
    @Test
    public void testNameConversion() {
        String name = "helloWorldW";
        var convertedName = TypeBuilder.convertToDSLName(name);
        assertEquals("hello_world_w", convertedName);
    }

    /** Test class for testing conversion into DSL datatype */
    @DSLType
    private class TestComponent {
        @DSLTypeMember public int intMember;

        @DSLTypeMember public String stringMember;

        @DSLTypeMember public Graph<String> graphMember;
    }

    /** Test class for testing conversion into DSL datatype */
    @DSLType
    private class ChainClass {
        @DSLTypeMember public TestComponent testComponentMember;

        @DSLTypeMember public String stringMember;
    }

    @DSLType
    private record TestRecord(
            @DSLTypeMember int comp1, @DSLTypeMember String comp2, @DSLTypeMember float comp3) {}

    @Test
    public void testSimpleClass() {
        TypeBuilder typeBuilder = new TypeBuilder();
        Scope scope = new Scope();
        var dslType = (AggregateType) typeBuilder.createTypeFromClass(scope, TestComponent.class);

        var stringMember = dslType.resolve("string_member");
        assertNotSame(stringMember, Symbol.NULL);
        assertEquals(BuiltInType.stringType, stringMember.getDataType());

        var intMember = dslType.resolve("int_member");
        assertNotSame(intMember, Symbol.NULL);
        assertEquals(BuiltInType.intType, intMember.getDataType());

        var graphMember = dslType.resolve("graph_member");
        assertNotSame(graphMember, Symbol.NULL);
        assertEquals(BuiltInType.graphType, graphMember.getDataType());
    }

    @Test
    public void testChainedClass() {
        TypeBuilder typeBuilder = new TypeBuilder();
        Scope scope = new Scope();
        var dslType = (AggregateType) typeBuilder.createTypeFromClass(scope, ChainClass.class);

        var testComponentMember = dslType.resolve("test_component_member");
        assertNotSame(testComponentMember, Symbol.NULL);

        var testComponentMemberType = testComponentMember.getDataType();
        assertEquals("test_component", testComponentMemberType.getName());

        var intMemberInTestComponent =
                ((AggregateType) testComponentMemberType).resolve("int_member");
        assertNotSame(intMemberInTestComponent, Symbol.NULL);
    }

    @Test
    public void testRecord() {
        TypeBuilder typeBuilder = new TypeBuilder();
        Scope scope = new Scope();
        var dslType = (AggregateType) typeBuilder.createTypeFromClass(scope, TestRecord.class);

        var comp1 = dslType.resolve("comp1");
        assertNotSame(comp1, Symbol.NULL);
        assertEquals(BuiltInType.intType, comp1.getDataType());

        var comp2 = dslType.resolve("comp2");
        assertNotSame(comp2, Symbol.NULL);
        assertEquals(BuiltInType.stringType, comp2.getDataType());

        var comp3 = dslType.resolve("comp3");
        assertNotSame(comp3, Symbol.NULL);
        assertEquals(BuiltInType.floatType, comp3.getDataType());
    }

    @Test
    public void testTypeAdapterRegister() {
        TypeBuilder tb = new TypeBuilder();
        tb.registerTypeAdapter(RecordBuilder.class, Scope.NULL);

        var adapter = tb.getRegisteredTypeAdapter(TestRecordComponent.class);
        assertNotNull(adapter);

        try {
            var object = adapter.invoke(null, "Hello");
            assertNotNull(object);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAdapterUsage() {
        TypeBuilder tb = new TypeBuilder();
        tb.registerTypeAdapter(RecordBuilder.class, Scope.NULL);
        var type = tb.createTypeFromClass(Scope.NULL, TestRecordUser.class);
        var memberSymbol = ((AggregateType) type).resolve("component_member");
        assertNotEquals(Symbol.NULL, memberSymbol);
        var membersDatatype = memberSymbol.getDataType();
        assertEquals(IType.Kind.PODAdapted, membersDatatype.getTypeKind());
    }

    @Test
    public void testExternalTypeMember() {
        TypeBuilder typeBuilder = new TypeBuilder();
        var dslType =
                (AggregateType)
                        typeBuilder.createTypeFromClass(
                                Scope.NULL, ComponentWithExternalTypeMember.class);

        assertNotSame(dslType, null);
        assertNotSame(dslType, Symbol.NULL);
    }

    @Test
    public void testInterfaceMember() {
        TypeBuilder typeBuilder = new TypeBuilder();
        var dslType =
                (AggregateType)
                        typeBuilder.createTypeFromClass(
                                Scope.NULL, ComponentWithInterfaceMember.class);

        assertNotSame(dslType, null);
        assertNotSame(dslType, Symbol.NULL);
    }
}