package Sources;
import com.assignment.grpc.* ;
import io.grpc.stub.StreamObserver;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.logging.Logger;

public class UserService extends userGrpc.userImplBase {
    private static final Logger logger = Logger.getLogger(UserService.class.getName());

    // Database connection information
    String url = "jdbc:mysql://localhost/user_profiles";
    String name = "root";
    String pass = "";

    @Override
    public void login(User.LoginRequest request, StreamObserver<User.Response> responseObserver) {
        String username = request.getUsername();
        String password = passwordHasher(request.getPassword());

        User.Response.Builder response = User.Response.newBuilder();

        try (Connection connection = DriverManager.getConnection(url, name, pass)) {
            String loginQuery = "SELECT password_hash FROM user_authentication WHERE username=?";
            PreparedStatement preparedStatement = connection.prepareStatement(loginQuery);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                String userpass = resultSet.getString("password_hash");
                if (password.equals(userpass)) {
                    response.setResponseCode(200).setMessage("OK! Login Successful");
                    logger.info("Login Successful for the user " + username);
                } else {
                    response.setResponseCode(400).setMessage("Bad Request !!!");
                    logger.info("Login failed for the user " + username);
                }
            } else {
                response.setResponseCode(400).setMessage("Bad Request !!!");
                logger.info("User not found: " + username);
            }

            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            logger.severe("Exception during login: " + ex.getMessage());
            response.setResponseCode(500).setMessage("Internal Server Error");
            responseObserver.onNext(response.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void register(User.RegistrationRequest request, StreamObserver<User.RegistrationResponse> responseObserver) {
        String registerPassword = passwordHasher(request.getPassword());
        String registerUsername = request.getUsername();

        User.RegistrationResponse.Builder registrationResponse = User.RegistrationResponse.newBuilder();

        try (Connection connection = DriverManager.getConnection(url, name, pass)) {
            String checkUserQuery = "SELECT username FROM user_authentication WHERE username=?";
            PreparedStatement checkUserStatement = connection.prepareStatement(checkUserQuery);
            checkUserStatement.setString(1, registerUsername);
            ResultSet checkUserResultSet = checkUserStatement.executeQuery();

            if (!checkUserResultSet.next()) {
                String registerQuery = "INSERT INTO user_authentication(username, password_hash, created_at) VALUES(?, ?, ?)";
                PreparedStatement preparedStatement = connection.prepareStatement(registerQuery);
                preparedStatement.setString(1, registerUsername);
                preparedStatement.setString(2, registerPassword);
                preparedStatement.setTimestamp(3, getCurrentTimestamp());

                int flag = preparedStatement.executeUpdate();
                if (flag == 1) {
                    registrationResponse.setResponseCode(201).setMessage("A new user by the username " + registerUsername + " was registered...");
                } else {
                    registrationResponse.setResponseCode(400).setMessage("Registration Failed. Please Try again...");
                }
            } else {
                registrationResponse.setResponseCode(400).setMessage("Username already taken. Please try again...");
            }

            responseObserver.onNext(registrationResponse.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            logger.severe("Exception during registration: " + ex.getMessage());
            registrationResponse.setResponseCode(500).setMessage("Internal Server Error");
            responseObserver.onNext(registrationResponse.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void createProfile(User.ProfileRequest request, StreamObserver<User.ProfileResponse> responseObserver) {
        User.ProfileResponse.Builder profileResponse = User.ProfileResponse.newBuilder();

        try (Connection connection = DriverManager.getConnection(url, name, pass)) {
            String createProfileQuery = "INSERT INTO user_profile(username, full_name, email, created_at, updated_at) VALUES(?, ?, ?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(createProfileQuery);
            preparedStatement.setString(1, request.getUsername());
            preparedStatement.setString(2, request.getFullName());
            preparedStatement.setString(3, request.getEmail());
            preparedStatement.setTimestamp(4, getCurrentTimestamp());
            preparedStatement.setTimestamp(5, getCurrentTimestamp());

            int insertedRows = preparedStatement.executeUpdate();

            if (insertedRows == 1) {
                profileResponse.setResponseCode(201).setMessage("Profile created successfully");
            } else {
                profileResponse.setResponseCode(400).setMessage("Failed to create profile");
            }

            responseObserver.onNext(profileResponse.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            logger.severe("Exception during profile creation: " + ex.getMessage());
            profileResponse.setResponseCode(500).setMessage("Internal Server Error");
            responseObserver.onNext(profileResponse.build());
            responseObserver.onCompleted();
        }
    }

    @Override
    public void updateProfile(User.UpdateProfileRequest request, StreamObserver<User.ProfileResponse> responseObserver) {
        User.ProfileResponse.Builder profileResponse = User.ProfileResponse.newBuilder();

        try (Connection connection = DriverManager.getConnection(url, name, pass)) {
            String updateProfileQuery = "UPDATE user_profile SET full_name = ?, email = ?, updated_at = ? WHERE username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(updateProfileQuery);
            preparedStatement.setString(1, request.getFullName());
            preparedStatement.setString(2, request.getEmail());
            preparedStatement.setTimestamp(3, getCurrentTimestamp());
            preparedStatement.setString(4, request.getUsername());

            int updatedRows = preparedStatement.executeUpdate();

            if (updatedRows == 1) {
                profileResponse.setResponseCode(200).setMessage("Profile updated successfully");
            } else {
                profileResponse.setResponseCode(400).setMessage("Failed to update profile");
            }

            responseObserver.onNext(profileResponse.build());
            responseObserver.onCompleted();
        } catch (Exception ex) {
            logger.severe("Exception during profile update: " + ex.getMessage());
            profileResponse.setResponseCode(500).setMessage("Internal Server Error");
            responseObserver.onNext(profileResponse.build());
            responseObserver.onCompleted();
        }
    }

    static String passwordHasher(String password) {
        // Hash the password using BCrypt
//        return BCrypt.hashpw(password, BCrypt.gensalt());
        return password;
    }

    private Timestamp getCurrentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}

