package accounting.service;

import java.time.LocalDateTime;
import java.util.Set;

public interface IManagment <T,R> {


    R registration(R user);

    R remove(String login);

    R getSupplier(String login);

    boolean updatePassword(String login, String password);

    boolean revokeAccount(String login);

    boolean activateAccount(String login);

    Set<String> getRoles(String login);

    boolean addRole(String login, String role);

    boolean removeRole(String login, String role);

    String getPasswordHash(String login);

    LocalDateTime getActivationDate(String login);

    R updateSupplierInfo(String login, T request);
}
