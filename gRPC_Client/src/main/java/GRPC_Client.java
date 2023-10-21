import com.assignment.grpc.User;
import com.assignment.grpc.userGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Scanner;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class GRPC_Client {
    private static final Logger logger = Logger.getLogger(GRPC_Client.class.getName());

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel managedChannel = ManagedChannelBuilder.forAddress("localhost", 8080).usePlaintext().build();
        userGrpc.userBlockingStub userBlockingStub = userGrpc.newBlockingStub(managedChannel);
        userGrpc.userStub userStub = userGrpc.newStub(managedChannel);

        Scanner scanner = new Scanner(System.in);
        int option;

        while (true) {
            displayMenu();
            option = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character

            switch (option) {
                case 1:
                    registerUser(userBlockingStub, scanner);
                    break;
                case 2:
                    login(userBlockingStub, scanner);
                    break;
                case 3:
                    createProfile(userStub, scanner);
                    break;
                case 4:
                    updateProfile(userStub, scanner);
                    break;
                case 5:
                    // Exit the loop and stop the program
                    managedChannel.shutdown();
                    return;
                default:
                    System.out.println("Invalid option. Please choose a valid option.");
            }
        }
    }

    public static void displayMenu() throws InterruptedException {
        sleep(100);
        System.out.println("Choose any option from below:");
        System.out.println("ENTER 1 to register as a new user: ");
        System.out.println("ENTER 2 to login to an existing User Account");
        System.out.println("ENTER 3 to create a User Profile");
        System.out.println("ENTER 4 to update User Profile");
        System.out.println("ENTER 6 to Stop Program");
        System.out.print("What's your choice? ");
    }

    private static void registerUser(userGrpc.userBlockingStub userBlockingStub, Scanner scanner) {
        System.out.print("What will be your username?: ");
        String username = scanner.nextLine();
        System.out.print("Set a strong password: ");
        String password = scanner.nextLine();

        User.RegistrationRequest request = User.RegistrationRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();

        User.RegistrationResponse response = userBlockingStub.register(request);
        logger.info("Response Code: " + response.getResponseCode());
        logger.info("Response Message: " + response.getMessage());

    }

    private static void login(userGrpc.userBlockingStub userBlockingStub, Scanner scanner) {
        System.out.print("Enter your username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your password: ");
        String password = scanner.nextLine();

        User.LoginRequest request = User.LoginRequest.newBuilder()
                .setUsername(username)
                .setPassword(password)
                .build();

        User.Response response = userBlockingStub.login(request);
        logger.info("Response Code: " + response.getResponseCode());
        logger.info("Response Message: " + response.getMessage());
    }

    private static void createProfile(userGrpc.userStub userStub, Scanner scanner) {
        System.out.print("What is your nickname?: ");
        String username = scanner.nextLine();
        System.out.print("What is your full-name: ");
        String fullName = scanner.nextLine();
        System.out.print("What is your e-mail address?: ");
        String email = scanner.nextLine();

        User.ProfileRequest request = User.ProfileRequest.newBuilder()
                .setUsername(username)
                .setFullName(fullName)
                .setEmail(email)
                .build();

        StreamObserver<User.ProfileResponse> responseObserver = new StreamObserver<User.ProfileResponse>() {
            @Override
            public void onNext(User.ProfileResponse response) {
                logger.info("Response Code: " + response.getResponseCode());
                logger.info("Response Message: " + response.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.severe("Sorry! Error during profile creation: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("Congratulations! Your profile has created successfully.");
            }
        };

        userStub.createProfile(request, responseObserver);
    }

    private static void updateProfile(userGrpc.userStub userStub, Scanner scanner) {
        System.out.print("Enter your given username: ");
        String username = scanner.nextLine();
        System.out.print("Enter your updated full name: ");
        String fullName = scanner.nextLine();
        System.out.print("Enter your updated email: ");
        String email = scanner.nextLine();

        User.UpdateProfileRequest request = User.UpdateProfileRequest.newBuilder()
                .setUsername(username)
                .setFullName(fullName)
                .setEmail(email)
                .build();

        StreamObserver<User.ProfileResponse> responseObserver = new StreamObserver<User.ProfileResponse>() {
            @Override
            public void onNext(User.ProfileResponse response) {
                logger.info("Response Code: " + response.getResponseCode());
                logger.info("Response Message: " + response.getMessage());
            }

            @Override
            public void onError(Throwable throwable) {
                logger.severe("Error during profile update: " + throwable.getMessage());
            }

            @Override
            public void onCompleted() {
                logger.info("Profile update completed.");
            }
        };

        userStub.updateProfile(request, responseObserver);
    }
}
