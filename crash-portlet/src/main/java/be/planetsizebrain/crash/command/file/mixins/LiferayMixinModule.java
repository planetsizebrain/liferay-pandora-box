package be.planetsizebrain.crash.command.file.mixins;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.liferay.portal.model.User;

public class LiferayMixinModule extends SimpleModule {

	public LiferayMixinModule() {
		super("Liferay Domain Mixins");
	}

	@Override
	public void setupModule(SetupContext context) {
		super.setupModule(context);

		context.setMixInAnnotations(User.class, UserMixin.class);
	}
}