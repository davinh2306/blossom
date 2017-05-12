package fr.mgargadennec.blossom.core.user;

import fr.mgargadennec.blossom.core.common.mapper.DTOMapper;
import fr.mgargadennec.blossom.core.common.service.GenericCrudServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Maël Gargadennnec on 03/05/2017.
 */
public class UserServiceImpl extends GenericCrudServiceImpl<UserDTO, User> implements UserService {
  private final static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

  private final PasswordEncoder passwordEncoder;
  private final UserDao userDao;
  private final UserMailService userMailService;

  public UserServiceImpl(UserDao dao, DTOMapper<User, UserDTO> mapper, ApplicationEventPublisher publisher, PasswordEncoder passwordEncoder, UserMailService userMailService) {
    super(dao, mapper, publisher);
    this.passwordEncoder = passwordEncoder;
    this.userDao = dao;
    this.userMailService = userMailService;
  }

  @Override
  public Optional<UserDTO> getByEmail(String email) {
    return Optional.ofNullable(mapper.mapEntity(this.userDao.getByEmail(email)));
  }

  @Override
  public Optional<UserDTO> getByIdentifier(String identifier) {
    return Optional.ofNullable(mapper.mapEntity(this.userDao.getByIdentifier(identifier)));
  }

  @Override
  public UserDTO updateActivation(long id, boolean activated) {
    return mapper.mapEntity(this.userDao.updateActivation(id, activated));
  }

  @Override
  public UserDTO updatePassword(Long id, String password) {
    return mapper.mapEntity(this.userDao.updatePassword(id, passwordEncoder.encode(password)));
  }

  @Override
  public UserDTO updateLastConnection(Long id, Date lastConnection) {
    return mapper.mapEntity(this.userDao.updateLastConnection(id,lastConnection));
  }

  @Override
  public void askPasswordChange(long userId) {
    UserDTO updatedUser = this.updatePassword(userId, passwordEncoder.encode(UUID.randomUUID().toString()));
    try {
      userMailService.sendChangePasswordEmail(updatedUser);
    } catch (Exception e) {
      logger.error("Can't send password change email to user {}", userId, e);
    }
  }


}