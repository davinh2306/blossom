package fr.mgargadennec.blossom.ui.web.administration.role;

import com.google.common.base.Strings;
import fr.mgargadennec.blossom.core.common.search.SearchEngineImpl;
import fr.mgargadennec.blossom.core.role.RoleCreateForm;
import fr.mgargadennec.blossom.core.role.RoleDTO;
import fr.mgargadennec.blossom.core.role.RolePrivilegeUpdateForm;
import fr.mgargadennec.blossom.core.role.RoleService;
import fr.mgargadennec.blossom.core.role.RoleUpdateForm;
import fr.mgargadennec.blossom.ui.menu.OpenedMenu;
import fr.mgargadennec.blossom.ui.stereotype.BlossomController;
import java.util.Locale;
import java.util.NoSuchElementException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Maël Gargadennnec on 05/05/2017.
 */
@BlossomController("/administration/roles")
@OpenedMenu("roles")
public class RolesController {

  private static final Logger logger = LoggerFactory.getLogger(RolesController.class);

  private final RoleService roleService;
  private final SearchEngineImpl<RoleDTO> searchEngine;

  public RolesController(RoleService roleService, SearchEngineImpl<RoleDTO> searchEngine) {
    this.roleService = roleService;
    this.searchEngine = searchEngine;
  }

  @GetMapping
  public ModelAndView getRolesPage(@RequestParam(value = "q", required = false) String q,
    @PageableDefault(size = 25) Pageable pageable, Model model) {
    return tableView(q, pageable, model, "roles/roles");
  }

  @GetMapping("/_list")
  public ModelAndView getRolesTable(@RequestParam(value = "q", required = false) String q,
    @PageableDefault(size = 25) Pageable pageable, Model model) {
    return tableView(q, pageable, model, "roles/table");
  }

  private ModelAndView tableView(String q, Pageable pageable, Model model, String viewName) {
    Page<RoleDTO> roles;

    if (Strings.isNullOrEmpty(q)) {
      roles = this.roleService.getAll(pageable);
    } else {
      roles = this.searchEngine.search(q, pageable).getPage();
    }

    model.addAttribute("roles", roles);
    model.addAttribute("q", q);

    return new ModelAndView(viewName, model.asMap());
  }

  @GetMapping("/_create")
  public ModelAndView getRoleCreatePage(Model model, Locale locale) {
    return this.createView(new RoleCreateForm(), model);
  }

  @PostMapping("/_create")
  public ModelAndView handleRoleCreateForm(
    @Valid @ModelAttribute("roleCreateForm") RoleCreateForm roleCreateForm,
    BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      return this.createView(roleCreateForm, model);
    }
    try {
      RoleDTO role = this.roleService.create(roleCreateForm);
      return new ModelAndView("redirect:../roles/" + role.getId());
    } catch (Exception e) {
      logger
        .error("Error on creating role, name " + roleCreateForm.getName() + " already exists ", e);
      return this.createView(roleCreateForm, model);
    }
  }

  private ModelAndView createView(RoleCreateForm roleCreateForm, Model model) {
    model.addAttribute("roleCreateForm", roleCreateForm);
    return new ModelAndView("roles/create", model.asMap());
  }

  @GetMapping("/{id}")
  public ModelAndView getRole(@PathVariable Long id, Model model, HttpServletRequest request) {
    RoleDTO role = this.roleService.getOne(id);
    if (role == null) {
      throw new NoSuchElementException(String.format("Role=%s not found", id));
    }
    model.addAttribute("role", role);
    return new ModelAndView("roles/role", "role", role);
  }

  @PostMapping("/{id}/_delete")
  public String deleteRole(@PathVariable Long id) {
    this.roleService.delete(this.roleService.getOne(id));
    return "redirect:..";
  }

  @GetMapping("/{id}/_informations")
  public ModelAndView getRoleInformations(@PathVariable Long id, HttpServletRequest request) {
    RoleDTO role = this.roleService.getOne(id);
    if (role == null) {
      throw new NoSuchElementException(String.format("Role=%s not found", id));
    }
    return this.viewRoleInformationView(role);
  }

  @GetMapping("/{id}/_informations/_edit")
  public ModelAndView getRoleInformationsForm(@PathVariable Long id, Model model,
    HttpServletRequest request) {
    RoleDTO role = this.roleService.getOne(id);
    if (role == null) {
      throw new NoSuchElementException(String.format("Role=%s not found", id));
    }
    return this.updateRoleInformationView(new RoleUpdateForm(role), model);
  }

  @PostMapping("/{id}/_informations/_edit")
  public ModelAndView handleRoleInformationsForm(@PathVariable Long id, Model model,
    HttpServletResponse response,
    @Valid @ModelAttribute("roleUpdateForm") RoleUpdateForm roleUpdateForm,
    BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      return this.updateRoleInformationView(roleUpdateForm, model);
    }

    RoleDTO role = this.roleService.getOne(id);
    if (role == null) {
      throw new NoSuchElementException(String.format("Role=%s not found", id));
    }
    role.setName(roleUpdateForm.getName());
    role.setDescription(roleUpdateForm.getDescription());
    RoleDTO updatedRole = this.roleService.update(id, role);

    return this.viewRoleInformationView(updatedRole);
  }

  private ModelAndView viewRoleInformationView(RoleDTO role) {
    return new ModelAndView("roles/roleinformations", "role", role);
  }

  private ModelAndView updateRoleInformationView(RoleUpdateForm roleUpdateForm, Model model) {
    return new ModelAndView("roles/roleinformations-edit", "roleUpdateForm", roleUpdateForm);
  }

  @GetMapping("/{id}/_privileges")
  public ModelAndView getRolePrivileges(@PathVariable Long id, Model model) {
    RoleDTO role = this.roleService.getOne(id);
    if (role == null) {
      throw new NoSuchElementException(String.format("Role=%s not found", id));
    }
    return this.viewRolePrivilegeView(role, model);
  }

  @GetMapping("/{id}/_privileges/_edit")
  public ModelAndView getRolePrivilegesForm(@PathVariable Long id, Model model) {
    RoleDTO role = this.roleService.getOne(id);
    if (role == null) {
      throw new NoSuchElementException(String.format("Role=%s not found", id));
    }
    return this.updateRolePrivilegesView(new RolePrivilegeUpdateForm(role), model);
  }

  @PostMapping("/{id}/_privileges/_edit")
  @ResponseStatus(HttpStatus.OK)
  public ModelAndView handleRolePrivilegesForm(@PathVariable Long id,
    @Valid @ModelAttribute("rolePrivilegeUpdateForm") RolePrivilegeUpdateForm rolePrivilegeUpdateForm,
    BindingResult bindingResult, Model model) {
    if (bindingResult.hasErrors()) {
      return this.updateRolePrivilegesView(rolePrivilegeUpdateForm, model);
    }

    RoleDTO role = this.roleService.getOne(id);
    if (role == null) {
      throw new NoSuchElementException(String.format("Role=%s not found", id));
    }
    role.setPrivileges(rolePrivilegeUpdateForm.getPrivileges());
    RoleDTO updatedRole = this.roleService.update(id, role);
    return this.viewRolePrivilegeView(updatedRole, model);
  }

  private ModelAndView viewRolePrivilegeView(RoleDTO role, Model model) {
    model.addAttribute("role", role);
    model.addAttribute("privileges", this.roleService.getAvailablePrivileges());
    return new ModelAndView("roles/roleprivileges", model.asMap());
  }

  private ModelAndView updateRolePrivilegesView(RolePrivilegeUpdateForm rolePrivilegeUpdateForm, Model model) {
    model.addAttribute("rolePrivilegeUpdateForm", rolePrivilegeUpdateForm);
    model.addAttribute("privileges", this.roleService.getAvailablePrivileges());
    return new ModelAndView("roles/roleprivileges-edit", model.asMap());
  }
}
