package com.ninebudget.service;

import com.ninebudget.model.ApplicationUser;
import com.ninebudget.model.Credential;
import com.ninebudget.model.InvalidUsernameOrPasswordException;
import com.ninebudget.model.dto.AccountDto;
import com.ninebudget.model.dto.ApplicationUserDto;
import com.ninebudget.model.dto.CredentialDto;
import com.ninebudget.model.mapper.AccountMapper;
import com.ninebudget.model.mapper.ApplicationUserMapper;
import com.ninebudget.model.mapper.CredentialMapper;
import com.ninebudget.repository.ApplicationUserRepository;
import com.ninebudget.util.RandomUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for managing users.
 */
@Service
@Transactional
public class UserService {
    private final Logger log = LoggerFactory.getLogger(UserService.class);

    private static final long PASSWORD_REST_TIMEOUT = 300; //In secs, 5 mins

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CredentialService credentialService;

    private final AccountMapper accountMapper;
    private final ApplicationUserMapper applicationUserMapper;
    private final CredentialMapper credentialMapper;

    public UserService(AccountMapper accountMapper, ApplicationUserMapper applicationUserMapper, CredentialMapper credentialMapper) {
        this.accountMapper = accountMapper;
        this.applicationUserMapper = applicationUserMapper;
        this.credentialMapper = credentialMapper;
    }

    public Optional<ApplicationUser> activateRegistration(String key) {
        log.debug("Activating Application User for activation key {}", key);

        return applicationUserRepository.findOneByActivationKey(key)
                .map(user -> {
                    // activate given user for the registration key.
                    user.setActivated(true);
                    user.setActivationKey(null);

                    log.debug("Activated user: {}", user);

                    return user;
                });
    }

    /**
     * Will Reset the User's password if it is not expired
     *
     * @param newPassword - New Password for User
     * @param key - Reset Key stored in the DB
     * @return - User if password is reset
     */
    public Optional<ApplicationUserDto> completePasswordReset(String newPassword, String key) {
        log.debug("Reset user password for reset key {}", key);

        Instant most = Instant.now().plusSeconds(PASSWORD_REST_TIMEOUT);
        Instant least = Instant.now().minusSeconds(PASSWORD_REST_TIMEOUT);

        return applicationUserRepository.findOneByResetKey(key)
                .filter(user -> user.getResetDate().isAfter(least) && user.getResetDate().isBefore(most))
                .map(user -> {
                    user.getCredential().setPassword(passwordEncoder.encode(newPassword));
                    user.setResetKey(null);
                    user.setResetDate(null);
                    return applicationUserMapper.toDto(user);
                });
    }

    /**
     * Allows a User's account to have their Password reset
     *
     * Will find by email and then check if they are active
     *
     * @param mail - email to check against
     * @return - User if exists
     */
    public Optional<ApplicationUserDto> requestPasswordReset(String mail) {
        log.debug("Reset user password requested for: {}", mail);

        return applicationUserRepository.findOneByEmailIgnoreCase(mail)
                .filter(ApplicationUser::getActivated)
                .map(user -> {
                    user.setResetKey(RandomUtil.generateResetKey());
                    user.setResetDate(Instant.now());
                    return applicationUserMapper.toDto(user);
                });
    }

    /**
     * Get one ApplicationUser by id.
     *
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ApplicationUserDto> findOneByEmailIgnoreCase(String email) {
        log.debug("Request to get Application User By Email: {}", email);

        return applicationUserRepository.findOneByEmailIgnoreCase(email)
                .map(applicationUserMapper::toDto);
    }

    /**
     * Get one ApplicationUser by id.
     *
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ApplicationUserDto> findOneByCredential(CredentialDto credential) throws Exception {
        log.debug("Request to get Application User By Credential: {}", credential);

        //Grab credential from DB
        Optional<CredentialDto> loaded = credentialService.findOneByUsername(credential.getUsername());

        return loaded.map(credentialDto -> applicationUserRepository.findOneByCredential(credentialMapper.toEntity(credentialDto))
                .map(applicationUserMapper::toDto)).orElse(null);
    }

    public void checkPassword(String givenPass, String passToCheck) throws InvalidUsernameOrPasswordException {
        /*
            Check to see if the Passwords match

            If not, error
         */
        if (!passwordEncoder.matches(givenPass, passToCheck)) {
            throw new InvalidUsernameOrPasswordException();
        }
    }

    private boolean removeNonActivatedUser(ApplicationUser existingApplicationUser) {
        if (existingApplicationUser.getActivated()) {
            return false;
        }
        applicationUserRepository.delete(existingApplicationUser);
        applicationUserRepository.flush();
        return true;
    }

    public ApplicationUserDto createUser(ApplicationUserDto applicationUserDTO) {
        ApplicationUser applicationUser = new ApplicationUser();

        applicationUser.setFirstName(applicationUserDTO.getFirstName());
        applicationUser.setLastName(applicationUserDTO.getLastName());
        if (applicationUserDTO.getEmail() != null) {
            applicationUser.setEmail(applicationUserDTO.getEmail().toLowerCase());
        }

        if (applicationUserDTO.getPhone() != null) {
            applicationUser.setPhone(applicationUserDTO.getPhone());
        }

        String encryptedPassword = passwordEncoder.encode(applicationUserDTO.getCredential().getPassword());
        Credential credential = new Credential();
        credential.setPassword(encryptedPassword);
        credential.setUsername(applicationUserDTO.getCredential().getUsername().toLowerCase());
        applicationUser.setCredential(credential);

        applicationUser.setResetKey(RandomUtil.generateResetKey());
        applicationUser.setActivationKey(RandomUtil.generateActivationKey());
        applicationUser.setResetDate(Instant.now());

        /*
           Grab account
         */
        AccountDto accountDto = getAccount(applicationUserDTO);
        applicationUser.setAccount(accountMapper.toEntity(accountDto));

        //User is only activated once they respond to email
        applicationUser.setActivated(false);

        applicationUser.setCreatedBy(applicationUserDTO.getCredential().getUsername());
        applicationUser.setLastModifiedBy(applicationUserDTO.getCredential().getUsername());

        applicationUserRepository.save(applicationUser);
        log.debug("Created Information for Application User: {}", applicationUser);

        return applicationUserMapper.toDto(applicationUser);
    }

    /**
     * Save an Application User.
     *
     * @param applicationUserDto the entity to save.
     * @return the persisted entity.
     */
    public ApplicationUserDto save(ApplicationUserDto applicationUserDto) {
        log.debug("Request to save Application User : {}", applicationUserDto);
        ApplicationUser applicationUser = applicationUserMapper.toEntity(applicationUserDto);
        applicationUser = applicationUserRepository.save(applicationUser);
        return applicationUserMapper.toDto(applicationUser);
    }

    public void delete(UUID id) {
        log.debug("Request to delete Application User : {}", id);
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //Only delete if User has access
        Optional<ApplicationUser> user = applicationUserRepository.findById(id);
        if (user.isPresent() && user.get().getAccount().getId().toString().equals(principal.getUsername())) {
            applicationUserRepository.deleteById(id);
        }
    }

    /**
     * Need to set account
     *
     * Try grabbing account object from user passed in first
     * If that doesn't exist, try currently logged in account.
     * If that doesn't work, last resort is null
     *
     * @param applicationUserDTO
     * @return AccountDto
     */
    private AccountDto getAccount(ApplicationUserDto applicationUserDTO){
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //Grab account from passed in User
        if(applicationUserDTO.getAccount() != null){
            return new AccountDto(applicationUserDTO.getAccount().getId());
        }

        //Grab by currently logged in User
        if(principal != null){
            return new AccountDto(UUID.fromString(principal.getUsername()));
        }

        return null;
    }
}
