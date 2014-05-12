package controllers;

import static play.data.Form.form;
import models.DateConstants;
import models.Guardian;
import models.Institution;
import models.Student;
import models.User;
import models.UserStatistics;
import play.Routes;
import play.data.Form;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.*;
import views.html.changePassword;
import views.html.login;
import views.html.moreinfo;
import views.html.signup;
public class Application extends Controller {

	// -- Authentication

	public static class Login {

		public String username;
		public String password;

		public String validate() {
			if (User.authenticate(username, password) == null) {
				return "Invalid username or password";
			}
			UserStatistics.recordLogin(username);
			return null;
		}
	}
	public static class SignUp {
		public String username;
		public String password;
		public String password2;
		public String email;
		public String role;

		public String validate() {
			if (!User.authenticateRegistration(username, password, password2,
					email, role)) {
				return "Registration invalid. Try again.";
			}
			User.createAndInsert(username, email, password, role);
			return null;
		}
	}
	public static class StudentRegistration {
		public String firstName;
		public String lastName;
		public int role;
		public String username;
		public int classyear;
		public int guardianid;

		public String validate() {			
			Student.createAndInsert(username, role, 348, firstName, lastName, classyear);
			return null;
		}
	}
	public static class ChangePassword {
		public String username;
		public String newPassword1;
		public String newPassword2;
		public String oldPassword;

		public String validate() {
			return User.changePassword(username, oldPassword, newPassword1,
					newPassword2);
		}
	}

	/**
	 * Login page.
	 */
	public static Result login() {
		return ok(login.render(form(Login.class)));
	}

	/**
	 * Handle login form submission.
	 */
	public static Result authenticate() {
		Form<Login> loginForm = form(Login.class).bindFromRequest();
		if (loginForm.hasErrors()) {
			return badRequest(login.render(loginForm));
		} else {
			session("username", loginForm.get().username);
			return redirect(routes.Projects.index());
		}
	}

	/**
	 * more Info page.
	 */
	public static Result moreinfo(String username) {
		char role = User.find.where().eq("username", username).findUnique().role;
		if (role == 'G') {
			return ok(moreinfo.render(username, form(models.Guardian.class)));
		} else if (role == 'S') {
			return ok(moreStudentInfo.render(username,
					form(StudentRegistration.class), Institution.find.all()));
		}
		session("username", username);
		return redirect(routes.Projects.index());
	}

	public static Result users() {
		return ok(users.render(Institution.find.byId(248), User.find.where().eq("removeDate", DateConstants.INFINITY.businessDate).findList()));
	}

	public static Result othermain() {
		return ok(othermain.render(Student.find.all(),User.find.byId(248)));
	}
	/**
	 * Add User info a user.
	 */
	public static Result moreInformation(String username) {
		User user = User.findByUsername(username);
		char role = user.role;
		if (role == 'G') {
			Form<Guardian> userForm = form(Guardian.class).bindFromRequest();
			if (userForm.hasErrors()) {
				return badRequest(moreinfo.render(username, userForm));
			} else {
				session("firstName", userForm.get().firstName);
				Guardian.create(userForm.get(), username).save();
				//return redirect(routes.Projects.index());
			}

		}
		else if (role == 'S') {
			Form<StudentRegistration> userForm = form(StudentRegistration.class).bindFromRequest();
			if (userForm.hasErrors()) {
				return badRequest(moreStudentInfo.render(username, userForm,Institution.find.all()));
			} else {
				session("firstName", userForm.get().firstName);
				//return redirect(routes.Projects.index());
			}

		}
		return redirect(routes.Projects.index());
	}

	/**
	 * Registration page.
	 */
	public static Result signup() {
		return ok(signup.render(form(SignUp.class)));
	}

	/**
	 * Handle registration form submission.
	 */
	public static Result authenticateSignUp() {
		Form<SignUp> signupform = form(SignUp.class).bindFromRequest();
		if (signupform.hasErrors()) {
			return badRequest(signup.render(signupform));
		} else {
			session("username", signupform.get().username);
			return redirect(routes.Application
					.moreInformation(signupform.get().username));
		}
	}

	/**
	 * Logout and clean the session.
	 */
	public static Result logout(String username) {
		UserStatistics.recordLogout(username);
		session().clear();
		flash("success", "You've been logged out");
		return redirect(routes.Application.login());
	}

	public static Result changePassword(String username) {
		return ok(changePassword.render(username, form(ChangePassword.class)));
	}

	/**
	 * Handle registration form submission.
	 */
	public static Result authenticateChangePassword(String username) {
		Form<ChangePassword> signupform = form(ChangePassword.class)
				.bindFromRequest();
		if (signupform.hasErrors()) {
			return badRequest(changePassword.render(username, signupform));
		} else {
			session("username", signupform.get().username);
			return redirect(routes.Projects.index());
		}
	}

	// -- Javascript routing

	public static Result javascriptRoutes() {
		response().setContentType("text/javascript");
		return ok(Routes.javascriptRouter(
				"jsRoutes",

				// Routes for Projects
				controllers.routes.javascript.Projects.add(),
				controllers.routes.javascript.Projects.delete(),
				controllers.routes.javascript.Projects.rename(),
				controllers.routes.javascript.Projects.addGroup(),
				controllers.routes.javascript.Projects.deleteGroup(),
				controllers.routes.javascript.Projects.renameGroup(),
				controllers.routes.javascript.Projects.addUser(),
				controllers.routes.javascript.Projects.removeUser(),

				// Routes for Tasks
				controllers.routes.javascript.Tasks.addFolder(),
				controllers.routes.javascript.Tasks.renameFolder(),
				controllers.routes.javascript.Tasks.deleteFolder(),
				controllers.routes.javascript.Tasks.index(),
				controllers.routes.javascript.Tasks.add(),
				controllers.routes.javascript.Tasks.update(),
				controllers.routes.javascript.Tasks.delete()

		));
	}

}
