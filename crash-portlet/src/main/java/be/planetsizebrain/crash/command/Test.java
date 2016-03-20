package be.planetsizebrain.crash.command;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.type.ArrayType;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public class Test {

	public static void main(String[] args) throws IOException {
		File file = new File("/java/personal/liferay/liferay-portal-6.2-ce-ga4/crash/test.csv");

		CsvMapper mapper = new CsvMapper();
//		mapper.enable(CsvParser.Feature.WRAP_AS_ARRAY);
//		CollectionType type = TypeFactory.defaultInstance().constructCollectionType(List.class, Map.class);
		CsvSchema schema = mapper.schemaFor(Map.class).withHeader();
		MappingIterator<Map<String, String>> it = mapper.readerFor(Map.class).with(schema).readValues(file);
//		MappingIterator<Object[]> it = mapper.readerFor(String[].class).readValues(file);
		while (it.hasNext()) {
//			String[] values = (String[]) it.next();
//			for (String value : values) {
//				System.out.print(value);
//				System.out.print(" ");
//			}
//			System.out.println("");
			System.out.println(it.next());
		}
	}
}