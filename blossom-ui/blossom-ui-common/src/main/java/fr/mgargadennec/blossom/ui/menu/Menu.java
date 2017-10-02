package fr.mgargadennec.blossom.ui.menu;

import fr.mgargadennec.blossom.ui.current_user.CurrentUser;
import java.util.Collection;

/**
 * Created by Maël Gargadennnec on 05/05/2017.
 */
public interface Menu {

  Collection<MenuItem> items();

  Collection<MenuItem> filteredItems(CurrentUser currentUser);

}
