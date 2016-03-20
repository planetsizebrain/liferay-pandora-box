package be.planetsizebrain.crash.command.file;

import static be.planetsizebrain.crash.command.file.Fields.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.crsh.command.Pipe;
import org.crsh.text.Color;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.liferay.portal.model.BaseModel;
import com.liferay.portal.model.User;
import com.liferay.portal.security.auth.CompanyThreadLocal;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.security.permission.PermissionCheckerFactoryUtil;
import com.liferay.portal.security.permission.PermissionThreadLocal;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

import be.planetsizebrain.crash.command.file.mixins.LiferayMixinModule;
import be.planetsizebrain.crash.command.file.mixins.UserMixin;

public class FileOutputPipe extends Pipe<Object, String> {

	private Format format;
	private Fields fields;
	private File file;

	private List elements = Lists.newArrayList();

	public FileOutputPipe(Format format, Fields fields, File file) {
		this.format = format;
		this.fields = fields;
		this.file = file;
	}

	@Override
	public void open() throws Exception {
//		User user = UserLocalServiceUtil.getUserByScreenName(PortalUtil.getDefaultCompanyId(), "joebloggs");
//		PermissionChecker permissionChecker = PermissionCheckerFactoryUtil.create(user);
//
//		PermissionThreadLocal.setPermissionChecker(permissionChecker);
//		CompanyThreadLocal.setCompanyId(user.getCompanyId());
//		PrincipalThreadLocal.setName(user.getUserId());
	}

	@Override
	public void provide(Object element) {
		elements.add(element);
	}

	@Override
	public void close() throws Exception {
		switch (format) {
			case JSON: writeJson(); break;
			case CSV: writeCsv(); break;
		}

		context.getWriter().print("Written " + elements.size() + " element(s) in format " + format + " to: ", Color.green);
		context.provide(file.getAbsolutePath());
	}

	private Class getElementClass() {
		if (elements.size() > 0) {
			Object element = elements.get(0);

			if (element instanceof BaseModel) {
//				if (LIFERAY == fields && JSON == format) {
				if (LIFERAY == fields) {
					return Map.class;
				} else {
					BaseModel model = (BaseModel) element;
					return model.getModelClass();
				}
			}

			return element.getClass();
		}

		return Object.class;
	}

	private void writeCsv() throws IOException {
		CollectionType type = TypeFactory.defaultInstance().constructCollectionType(List.class, getElementClass());

		CsvSchema schema;
		CsvMapper csvMapper = new CsvMapper();

		switch (fields) {
			case ALL:
				Object element = elements.get(0);
				if (element instanceof Map) {
					Map map = (Map) element;
					CsvSchema.Builder builder = CsvSchema.builder();
					for (String column : (Set<String>) map.keySet()) {
						builder.addColumn(column);
					}
					schema = builder.build().withHeader();
				} else {
					schema = csvMapper.schemaFor(getElementClass()).withHeader();
				}

				csvMapper.
					writer(schema).
					forType(type).
					writeValue(file, elements);

				break;
			case LIFERAY:
//				schema = csvMapper.schemaFor(getElementClass()).withHeader();
				List transformedElements = Lists.newArrayList(
					Iterables.transform(elements, new Function() {
						@Override
						public Object apply(Object element) {
						BaseModel model = (BaseModel) element;
						return model.getModelAttributes();
						}
					})
				);
				BaseModel model = (BaseModel) elements.get(0);
				CsvSchema.Builder builder = CsvSchema.builder();
				for (String column : (Set<String>) model.getModelAttributes().keySet()) {
					builder.addColumn(column);
				}
				schema = builder.build().withHeader();

				csvMapper.
					writer(schema).
					forType(type).
					writeValue(file, transformedElements);
				break;
			case MIXIN:
				csvMapper.
					registerModule(new LiferayMixinModule()).
//					addMixIn(User.class, UserMixin.class).
					disable(MapperFeature.AUTO_DETECT_CREATORS).
					disable(MapperFeature.AUTO_DETECT_FIELDS).
					disable(MapperFeature.AUTO_DETECT_GETTERS).
					disable(MapperFeature.AUTO_DETECT_IS_GETTERS);

//				Class mixin = csvMapper.findMixInClassFor(getElementClass());
				schema = csvMapper.schemaFor(getElementClass()).withHeader();

				csvMapper.
					writer(schema).
					forType(type).
					writeValue(file, elements);

				break;
		}
	}

	private void writeJson() throws IOException {
		CollectionType type = TypeFactory.defaultInstance().constructCollectionType(List.class, getElementClass());

		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.
			enable(SerializationFeature.INDENT_OUTPUT).
			disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

		switch (fields) {
			case ALL:
				objectMapper.
					writerWithDefaultPrettyPrinter().
					forType(type).
					writeValue(file, elements);

				break;
			case LIFERAY:
				List transformedElements = Lists.newArrayList(
					Iterables.transform(elements, new Function() {
						@Override
						public Object apply(Object element) {
						BaseModel model = (BaseModel) element;
						return model.getModelAttributes();
						}
					})
				);
				objectMapper.
					writerWithDefaultPrettyPrinter().
					forType(type).
					writeValue(file, transformedElements);
				break;
			case MIXIN:
				objectMapper.
					addMixIn(User.class, UserMixin.class).
					disable(MapperFeature.AUTO_DETECT_CREATORS).
					disable(MapperFeature.AUTO_DETECT_FIELDS).
					disable(MapperFeature.AUTO_DETECT_GETTERS).
					disable(MapperFeature.AUTO_DETECT_IS_GETTERS).
					writerWithDefaultPrettyPrinter().
					forType(type).
					writeValue(file, elements);

				break;
		}
	}
}