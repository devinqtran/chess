package service;

import dataaccess.*;
import model.AuthData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for UserService
 */
public class UserServiceTests {

    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;
}