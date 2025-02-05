package accounting.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "suppliers")

public class SupplierAccount {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Setter(value = AccessLevel.NONE)
	private Long id;

	@Column(nullable = false, unique = true, length = 50)
	private String login;
	
	@Column(nullable = false, length = 255)
	private String hash;
	
	@Column(nullable = false, length = 50)
	private String email;
	
	@Column(nullable = false, length = 50)
	private String companyName;
	
	@Column(nullable = false, length = 255)
	private String companyAddress;
	
	@Column(nullable = false, length = 20)
	private String taxId;
	
	@Column(nullable = false, length = 50)
	private String contactPerson;
	
	@Column(nullable = false, length = 20)
	private String phone;

	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_account_id"))
	@Column(name = "role")
	private HashSet<String> roles = new HashSet<>();
	
	@Column(nullable = false)
	private LocalDateTime activationDate = LocalDateTime.now();
	
	@Column(nullable = false)
	private boolean revoked = false;
	
	@CollectionTable(name = "supplier_last_hash", joinColumns = @JoinColumn(name = "supplier_id"))
	@Column(name = "last_hash")
	private LinkedList<String> lastHash = new LinkedList<>();
	
	@ManyToMany(mappedBy = "suppliers")
	private Set<CustomerAccount> customers = new HashSet<>();

	public SupplierAccount(String login, String hash, String email, String companyName, String companyAddress,
			String taxId, String contactPerson, String phone) {
		super();
		this.login = login;
		this.hash = hash;
		this.email = email;
		this.companyName = companyName;
		this.companyAddress = companyAddress;
		this.taxId = taxId;
		this.contactPerson = contactPerson;
		this.phone = phone;
		roles.add("SUPPLIER");
		activationDate = LocalDateTime.now();
	}

}
