package be.planetsizebrain.crash.command.file.mixins;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.liferay.portal.model.BaseModel;

public interface BaseModelMixin extends BaseModel {

	@JsonIgnore
	long getColumnBitmask();
}
