package core.mvc.tobe;

import core.di.ApplicationContext;
import next.dao.UserDao;
import next.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import support.test.DBInitializer;

import static org.assertj.core.api.Assertions.assertThat;

public class AnnotationHandlerMappingTest {
    private AnnotationHandlerMapping handlerMapping;
    private UserDao userDao;

    @BeforeEach
    public void setup() {
        handlerMapping = new AnnotationHandlerMapping(new ApplicationContext("next"));
        handlerMapping.initialize();

        DBInitializer.initialize();
        userDao = UserDao.getInstance();
    }

    @Test
    public void create_find() throws Exception {
        User user = new User("pobi", "password", "포비", "pobi@nextstep.camp");
        createUser(user);
        assertThat(userDao.findByUserId(user.getUserId())).isEqualTo(user);

        MockHttpServletRequest request = login(user);
        request.setMethod("GET");
        request.setRequestURI("/users");
        request.setParameter("userId", user.getUserId());
        MockHttpServletResponse response = new MockHttpServletResponse();

        HandlerExecution execution = (HandlerExecution) handlerMapping.getHandler(request);
        execution.handle(request, response);

        assertThat(request.getSession().getAttribute("user")).isEqualTo(user);
    }

    private void createUser(User user) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/users");
        request.setParameter("userId", user.getUserId());
        request.setParameter("password", user.getPassword());
        request.setParameter("name", user.getName());
        request.setParameter("email", user.getEmail());

        MockHttpServletResponse response = new MockHttpServletResponse();
        HandlerExecution execution = (HandlerExecution) handlerMapping.getHandler(request);
        execution.handle(request, response);
    }

    private MockHttpServletRequest login(User user) throws Exception {
        final MockHttpServletRequest request = new MockHttpServletRequest("POST", "/users/login");
        request.setParameter("userId", user.getUserId());
        request.setParameter("password", user.getPassword());

        final MockHttpServletResponse response = new MockHttpServletResponse();
        final HandlerExecution execution = (HandlerExecution) handlerMapping.getHandler(request);

        execution.handle(request, response);
        return request;
    }
}
