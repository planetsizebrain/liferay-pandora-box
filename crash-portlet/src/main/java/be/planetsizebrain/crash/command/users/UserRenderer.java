package be.planetsizebrain.crash.command.users;

import java.util.Iterator;
import java.util.List;

import org.crsh.text.Color;
import org.crsh.text.Decoration;
import org.crsh.text.LineRenderer;
import org.crsh.text.Renderer;
import org.crsh.text.ui.LabelElement;
import org.crsh.text.ui.Overflow;
import org.crsh.text.ui.RowElement;
import org.crsh.text.ui.TableElement;
import org.crsh.util.Utils;

import com.liferay.portal.model.User;

public class UserRenderer extends Renderer<User> {

	@Override
	public Class<User> getType() {
		return User.class;
	}

	@Override
	public LineRenderer renderer(Iterator<User> stream) {
		TableElement table = new TableElement(1,2,2,2).overflow(Overflow.HIDDEN).rightCellPadding(1);

		// Header
		table.add(
				new RowElement().style(Decoration.bold.fg(Color.black).bg(Color.white)).add(
				"ID",
				"NAME",
				"SCREENNAME",
				"EMAILADDRESS"
			)
		);

		List<User> users = Utils.list(stream);
		for (User user : users) {
			table.row(
				new LabelElement(user.getUserId()),
				new LabelElement(user.getFullName()),
				new LabelElement(user.getScreenName()),
				new LabelElement(user.getEmailAddress())
			);
		}

		return table.renderer();
	}
}