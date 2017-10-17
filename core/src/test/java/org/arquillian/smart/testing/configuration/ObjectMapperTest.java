package org.arquillian.smart.testing.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.arquillian.smart.testing.configuration.ObjectMapper.mapToObject;
import static org.arquillian.smart.testing.configuration.ObjectMapperTest.TestEnum.FOO;
import static org.assertj.core.api.Assertions.assertThat;

public class ObjectMapperTest {

    private Map<String, Object> map;

    @Before
    public void initMap() {
        map = new HashMap<>();
    }

    @Test
    public void should_set_integer_to_object() {
        // given
        map.put("i", 10);

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("i", 10);
    }

    @Test
    public void should_set_double_to_object() {
        // given
        map.put("d", 10.0);

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("d", 10.0);
    }

    @Test
    public void should_set_boolean_to_object() {
        // given
        map.put("b", true);

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("b", true);
    }

    @Test
    public void should_set_string_to_object() {
        // given
        map.put("s", "Hello");

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("s", "Hello");
    }

    @Test
    public void should_set_list_of_string_as_string_to_object() {
        // given
        map.put("s", Arrays.asList("foo", "bar"));

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("s","foo, bar");
    }

    @Test
    public void should_set_comma_separated_string_as_array_to_object() {
        // given
        map.put("as", "hello, bar");

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("as", new String[] {"hello", "bar"});
    }

    @Test
    public void should_set_list_to_object() {
        // given
        map.put("l", Arrays.asList("foo", "bar"));

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        assertThat(testObject).hasFieldOrPropertyWithValue("l", Arrays.asList("foo", "bar"));
    }

    @Test
    public void should_set_enum_to_object() {
        // given
        map.put("e", "foo");

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("e", FOO);
    }

    @Test
    public void should_set_nested_object_to_object() {
        // given
        Map<String, Object> innerObjectMap = new HashMap<>();
        innerObjectMap.put("b", true);
        innerObjectMap.put("as", Arrays.asList("foo", "bar"));
        innerObjectMap.put("l", Arrays.asList("foo", "bar"));
        map.put("dummyObject", innerObjectMap);

        final DummyObject dummyObject = new DummyObject();
        dummyObject.setB(true);
        dummyObject.setAs(new String[] {"foo", "bar"});
        dummyObject.setL(Arrays.asList("foo", "bar"));

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).isNotNull();
        assertThat(testObject.dummyObject).hasNoNullFieldsOrProperties();
        assertThat(testObject.dummyObject).isEqualToComparingFieldByField(dummyObject);
    }

    @Test
    public void should_set_map_to_object() {
        // given
        final Map<String, String> stringMap = Stream.of("foo", "bar")
            .collect(Collectors.toMap(Function.identity(), Function.identity()));
        map.put("m", stringMap);

        // when
        final TestObject testObject = mapToObject(TestObject.class, map);

        // then
        assertThat(testObject).hasFieldOrPropertyWithValue("m", stringMap);
    }

    @Test
    public void should_return_default_value_for_empty_map() {
        // given
        final TestObject expected = new TestObject();
        expected.setS(null);
        expected.setAs(new String[0]);
        expected.setB(false);
        expected.setD(0.0);
        expected.setE(FOO);
        expected.setI(0);
        expected.setL(Collections.emptyList());
        expected.setM(Collections.emptyMap());

        final DummyObject dummyObject = new DummyObject();
        dummyObject.setB(false);
        dummyObject.setL(Collections.emptyList());
        dummyObject.setAs(new String[0]);

        expected.setDummyObject(dummyObject);

        // when
        final TestObject testObject = mapToObject(TestObject.class, Collections.emptyMap());

        // then
        assertThat(testObject).isEqualToComparingFieldByFieldRecursively(expected);
    }

    @After
    public void clearMap() {
        map.clear();
    }

    static class TestObject implements ConfigurationSection {
        private String s;
        private int i;
        private double d;
        private boolean b;
        private String[] as;
        private List<String> l;
        private Map<String, String> m;
        private TestEnum e;

        private DummyObject dummyObject;

        public void setS(String s) {
            this.s = s;
        }

        public void setI(Integer i) {
            this.i = i;
        }

        public void setD(Double d) {
            this.d = d;
        }

        public void setB(Boolean b) {
            this.b = b;
        }

        public void setAs(String[] as) {
            this.as = as;
        }

        public void setL(List<String> l) {
            this.l = l;
        }

        public void setE(TestEnum e) {
            this.e = e;
        }

        public void setM(Map<String, String> m) {
            this.m = m;
        }

        public void setDummyObject(DummyObject dummyObject) {
            this.dummyObject = dummyObject;
        }

        @Override
        public List<ConfigurationItem> registerConfigurationItems() {
            List<ConfigurationItem> configItems = new ArrayList<>();
            configItems.add(new ConfigurationItem("i", 0));
            configItems.add(new ConfigurationItem("s"));
            configItems.add(new ConfigurationItem("d", 0.0));
            configItems.add(new ConfigurationItem("b", false));
            configItems.add(new ConfigurationItem("l", Collections.EMPTY_LIST));
            configItems.add(new ConfigurationItem("m", Collections.EMPTY_MAP));
            configItems.add(new ConfigurationItem("as", new String[0]));
            configItems.add(new ConfigurationItem("e", TestEnum.FOO));

            final DummyObject dummyObject = new DummyObject();
            dummyObject.setB(false);
            dummyObject.setL(Collections.emptyList());
            dummyObject.setAs(new String[0]);
            configItems.add(new ConfigurationItem("dummyObject", dummyObject));

            return configItems;
        }
    }

    static class DummyObject implements ConfigurationSection {
        private boolean b;
        private String[] as;
        private List<String> l;

        public void setB(boolean b) {
            this.b = b;
        }

        public void setAs(String[] as) {
            this.as = as;
        }

        public void setL(List<String> l) {
            this.l = l;
        }

        @Override
        public List<ConfigurationItem> registerConfigurationItems() {
            List<ConfigurationItem> configItems = new ArrayList<>();
            configItems.add(new ConfigurationItem("b", false));
            configItems.add(new ConfigurationItem("as", new String[0]));
            configItems.add(new ConfigurationItem("l", Collections.EMPTY_LIST));

            return configItems;
        }
    }

    enum TestEnum {
        FOO,
        BAR
    }
}
