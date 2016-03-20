import static be.planetsizebrain.crash.command.file.Fields.*;
import static be.planetsizebrain.crash.command.file.Format.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.crsh.cli.*;
import org.crsh.command.BaseCommand;
import org.crsh.command.InvocationContext;
import org.crsh.command.Pipe;
import org.crsh.text.Color;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import be.planetsizebrain.crash.command.file.Fields;
import be.planetsizebrain.crash.command.file.FileOutputPipe;
import be.planetsizebrain.crash.command.file.Format;

@Usage("File operations")
public class file extends BaseCommand {

	@Usage("read data from file")
	@Man("A JSON file must only contain a simple array of JSON objects and a CSV file must have a header.")
	@Command
	public void read(
			@Usage("the format of the output (JSON or CSV, default is JSON)")
			@Option(names = {"f", "format"})
			final Format format,
			@Argument @Required
			File file,
			InvocationContext<Map> context) throws Exception {

		int count = 0;

		switch (format) {
			case JSON:
				ObjectMapper objectMapper = new ObjectMapper();
				List<Map<String, String>> elements = objectMapper.readValue(file, new TypeReference<List<Map<String, String>>>(){});
				for (Map<String, String> element : elements) {
					context.provide(element);
					count++;
				}

				break;
			case CSV:
				CsvMapper mapper = new CsvMapper();
				CsvSchema schema = mapper.schemaFor(Map.class).withHeader();
				MappingIterator<Map<String, String>> it = mapper.readerFor(Map.class).with(schema).readValues(file);
				while (it.hasNext()) {
					context.provide(it.next());
					count++;
				}

				break;
		}

		context.getWriter().println("Read " + count + " element(s) from file " + file + " in format " + format, Color.green);
	}

	@Usage("write data to file")
	@Command
	public Pipe<Object, String> write(
			@Usage("the format of the output (JSON or CSV, default is JSON)")
			@Option(names = {"f", "format"})
			Format format,
			@Usage("the fields of the object that need to be output (LIFERAY, MIXIN or ALL, default is LIFERAY)")
			@Option(names = {"fields"})
			Fields fields,
			@Argument @Required
			final File file) {

		final Format outputFormat = format != null ? format : JSON;
		final Fields outputFields = fields != null ? fields : LIFERAY;

		return new FileOutputPipe(outputFormat, outputFields, file);
	}
}