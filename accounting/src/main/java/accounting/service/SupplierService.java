package accounting.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import accounting.dto.RolesResponseDto;
import accounting.dto.SupplierRequestDto;
import accounting.dto.SupplierResponseDto;
import accounting.dto.exceptions.AccountActivationException;
import accounting.dto.exceptions.AccountRevokeException;
import accounting.dto.exceptions.PasswordNotValidException;
import accounting.dto.exceptions.RoleExistsException;
import accounting.dto.exceptions.RoleNotExistsException;
import accounting.dto.exceptions.UserExistsException;
import accounting.dto.exceptions.UserNotFoundException;
import accounting.entity.SupplierAccount;
import accounting.repository.SupplierRepository;
import jakarta.transaction.Transactional;

public class SupplierService implements ISupplierManagement {

	@Autowired
	SupplierRepository repo;

	@Autowired
	PasswordEncoder encoder;

	@Value("${password_length:8}")
	private int passwordLength;

	@Value("${n_last_hash:3}")
	private int n_last_hash;

	@Override
	public SupplierResponseDto registration(SupplierRequestDto user) {

		if (repo.findByLogin(user.login()).isPresent()) {
			throw new UserExistsException(user.login());
		}
		if (!isPasswordValid(user.password()))
			throw new PasswordNotValidException(user.password());

		String hashedPassword = encoder.encode(user.password());

		SupplierAccount farmer = SupplierAccount.of(user);
		farmer.setHash(hashedPassword);

		return SupplierResponseDto.build(farmer);
	}

	private boolean isPasswordValid(String password) {
		return password.length() >= passwordLength;
	}

	@Transactional
	@Override
	public SupplierResponseDto removeUser(String login) {
		SupplierAccount farmer = getSupplierAccount(login);
		repo.deleteByLogin(login);

		return SupplierResponseDto.build(farmer);
	}

	private SupplierAccount getSupplierAccount(String login) {
		return repo.findByLogin(login).orElseThrow(() -> new UserNotFoundException(login));
	}

	@Override
	public SupplierResponseDto getUser(String login) {
		SupplierAccount farmer = getSupplierAccount(login);
		return SupplierResponseDto.build(farmer);
	}

	@Transactional
	@Override
	public boolean updatePassword(String login, String newPassword) {
		if (newPassword == null || !isPasswordValid(newPassword)) {
			throw new PasswordNotValidException(newPassword);
		}
		SupplierAccount farmer = getSupplierAccount(login);
		if (encoder.matches(newPassword, farmer.getHash()))
			throw new PasswordNotValidException(newPassword);

		LinkedList<String> lastHash = farmer.getLastHash();
		if (isPasswordFromLast(newPassword, lastHash)) {
			throw new PasswordNotValidException(newPassword);
		}

		if (lastHash.size() == n_last_hash)
			lastHash.removeFirst();
		lastHash.add(farmer.getHash());
		farmer.setHash(encoder.encode(newPassword));
		farmer.setActivationDate(LocalDateTime.now());
		repo.save(farmer);
		return true;
	}

	private boolean isPasswordFromLast(String newPassword, LinkedList<String> lastHash) {
		return lastHash.stream().anyMatch(p -> encoder.matches(newPassword, p));

	}

	@Transactional
	@Override
	public boolean updateUser(String login, SupplierRequestDto user) {

		SupplierAccount farmer = getSupplierAccount(login);

		if (user.email() != null)
			farmer.setEmail(user.email());
		if (user.companyName() != null)
			farmer.setCompanyName(user.companyName());
		if (user.companyAddress() != null)
			farmer.setCompanyAddress(user.companyAddress());
		if (user.taxId() != null)
			farmer.setTaxId(user.taxId());
		if (user.contactPerson() != null)
			farmer.setContactPerson(user.contactPerson());
		if (user.phone() != null)
			farmer.setPhone(user.phone());

		if (user.password() != null) {
			String hashedPassword = encoder.encode(user.password());
			farmer.setHash(hashedPassword);
		}

		repo.save(farmer);
		return true;
	}

	@Transactional
	@Override
	public boolean revokeAccount(String login) {
		SupplierAccount farmer = getSupplierAccount(login);
		if (farmer.isRevoked()) {
			throw new AccountRevokeException(login);
		}
		farmer.setRevoked(true);
		repo.save(farmer);
		return true;
	}

	@Transactional
	@Override
	public boolean activateAccount(String login) {
		SupplierAccount farmer = getSupplierAccount(login);
		if (!farmer.isRevoked())
			throw new AccountActivationException(login);
		farmer.setRevoked(false);
		farmer.setActivationDate(LocalDateTime.now());
		repo.save(farmer);
		return true;
	}

	@Override
	public RolesResponseDto getRoles(String login) {
		SupplierAccount farmer = getSupplierAccount(login);
		return farmer.isRevoked() ? null : new RolesResponseDto(login, farmer.getRoles());
	}

	@Transactional
	@Override
	public boolean addRole(String login, String role) {
		SupplierAccount farmer = getSupplierAccount(login);

		HashSet<String> roles = farmer.getRoles();
		if (roles.contains(role))
			throw new RoleExistsException(role);
		roles.add(role);
		repo.save(farmer);
		return true;

	}

	@Transactional
	@Override
	public boolean removeRole(String login, String role) {
		SupplierAccount farmer = getSupplierAccount(login);

		HashSet<String> roles = farmer.getRoles();
		if (!roles.contains(role))
			throw new RoleNotExistsException(role);
		roles.remove(role);
		repo.save(farmer);
		return true;
	}

	@Override
	public String getPasswordHash(String login) {
		SupplierAccount farmer = getSupplierAccount(login);
		return farmer.isRevoked() ? null : farmer.getHash();
	}

	@Override
	public LocalDateTime getActivationDate(String login) {
		SupplierAccount farmer = getSupplierAccount(login);
		return farmer.isRevoked() ? null : farmer.getActivationDate();
	}

}
