package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;

import org.example.springboot.entity.User;
import org.example.springboot.DTO.UserPasswordUpdateDTO;
import org.example.springboot.DTO.UserRegisterDTO;
import org.example.springboot.entity.Doctor;
import org.example.springboot.entity.Patient;
import org.example.springboot.enumClass.AccountStatus;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.mapper.DoctorMapper;
import org.example.springboot.mapper.PatientMapper;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    @Resource
    private UserMapper userMapper;
    
    @Resource
    private DoctorMapper doctorMapper;
    
    @Resource
    private PatientMapper patientMapper;
    
    @Value("${user.defaultPassword}")
    private String DEFAULT_PWD;

    @Resource
    private PasswordEncoder bCryptPasswordEncoder;

    public User getByEmail(String email) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
        if (user == null) {
            throw new ServiceException("邮箱不存在");
        }
        return user;
    }

    public User login(User user) {
        User dbUser = getByUsername(user.getUsername());
        // 用户存在性检查已经在 getByUsername 中处理
        if (dbUser.getStatus() == AccountStatus.PENDING_REVIEW.getValue()) {
            throw new ServiceException("账号正在审核");
        }
        if (dbUser.getStatus() == AccountStatus.REVIEW_FAILED.getValue()) {
            throw new ServiceException("账号审核不通过，请修改个人信息");
        }
        if (dbUser.getStatus() == AccountStatus.DISABLED.getValue()) {
            throw new ServiceException("账号已被禁用，请联系管理员");
        }
        if (!bCryptPasswordEncoder.matches(user.getPassword(), dbUser.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        

        String token = JwtTokenUtils.genToken(String.valueOf(dbUser.getId()), dbUser.getPassword());
        

        dbUser.setToken(token);
        return dbUser;
    }
    
    /**
     * 用户注册
     */
    @Transactional
    public User register(UserRegisterDTO registerDTO) {
        // 检查用户名是否存在
        if (userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, registerDTO.getUsername())
            ) != null) {
            throw new ServiceException("用户名已存在");
        }
        
        // 检查邮箱是否被使用
        if (userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, registerDTO.getEmail())
            ) != null) {
            throw new ServiceException("邮箱已被使用");
        }
        
        User user = new User();
        BeanUtils.copyProperties(registerDTO, user);
        
        // 加密密码
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        
        // 设置默认状态
        user.setStatus(AccountStatus.ENABLED.getValue());
        
        // 设置默认头像
        if (StringUtils.isBlank(user.getAvatar())) {
            user.setAvatar("/img/default_avatar.png");
        }
        
        if (userMapper.insert(user) <= 0) {
            throw new ServiceException("用户注册失败");
        }
        
        // 生成token
        String token = JwtTokenUtils.genToken(String.valueOf(user.getId()), user.getPassword());
        user.setToken(token);
        
        return user;
    }

    public List<User> getUserByRole(String roleCode) {
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>()
                .eq(User::getRoleCode, roleCode)
        );
        if (users.isEmpty()) {
            throw new ServiceException("未找到该角色的用户");
        }
        return users;
    }

    public void createUser(User user) {
        // 检查用户名是否存在
        if (userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
            ) != null) {
            throw new ServiceException("用户名已存在");
        }
        
        // 检查邮箱是否被使用
        if (userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, user.getEmail())
            ) != null) {
            throw new ServiceException("邮箱已被使用");
        }


        user.setPassword(StringUtils.isNotBlank(user.getPassword()) ? user.getPassword() : DEFAULT_PWD);
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        
        if (userMapper.insert(user) <= 0) {
            throw new ServiceException("用户创建失败");
        }
    }

    public void updateUser(Long id, User user) {
        // 检查用户是否存在
        if (userMapper.selectById(id) == null) {
            throw new ServiceException("要更新的用户不存在");
        }
        
        // 检查新用户名是否与其他用户重复
        if (user.getUsername() != null) {
            User existUser = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, user.getUsername())
            );
            if (existUser != null && !existUser.getId().equals(id)) {
                throw new ServiceException("新用户名已被使用");
            }
        }
        
        user.setId(id);
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("用户更新失败");
        }
    }

    public User getByUsername(String username) {
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    /**
     * 批量删除用户
     * 在删除前检查每个用户是否被医生或患者关联
     */
    @Transactional
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            // 转换为Long类型
            Long userId = Long.valueOf(id);
            
            // 检查用户是否存在
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new ServiceException("ID为" + userId + "的用户不存在");
            }
            
            // 检查用户是否被医生关联
            Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>()
                    .eq(Doctor::getUserId, userId)
            );
            if (doctor != null) {
                throw new ServiceException("ID为" + userId + "的用户已关联医生信息（" + doctor.getName() + "），不能删除");
            }
            
            // 检查用户是否被患者关联
            Patient patient = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>()
                    .eq(Patient::getUserId, userId)
            );
            if (patient != null) {
                throw new ServiceException("ID为" + userId + "的用户已关联患者信息（" + patient.getName() + "），不能删除");
            }
        }
        
        // 所有用户检查通过后，执行批量删除
        if (userMapper.deleteByIds(ids) <= 0) {
            throw new ServiceException("批量删除失败");
        }
    }

    public List<User> getUserList() {
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        if (users.isEmpty()) {
            throw new ServiceException("未找到任何用户");
        }
        return users;
    }

    public User getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        return user;
    }

    public Page<User> getUsersByPage(String username,  String name, String roleCode, Integer currentPage, Integer size) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like(User::getUsername, username);
        }

        if (StringUtils.isNotBlank(name)) {
            queryWrapper.like(User::getName, name);
        }
        if (StringUtils.isNotBlank(roleCode)) {
            queryWrapper.eq(User::getRoleCode, roleCode);
        }
        
        return userMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
    }

    public void updatePassword(Long id, UserPasswordUpdateDTO update) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 验证旧密码
        if (!bCryptPasswordEncoder.matches(update.getOldPassword(), user.getPassword())) {
            throw new ServiceException("原密码错误");
        }
        
        // 更新新密码
        user.setPassword(bCryptPasswordEncoder.encode(update.getNewPassword()));
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("密码修改失败");
        }
    }

    /**
     * 忘记密码 - 根据邮箱重置密码
     * @param email 用户邮箱
     * @param newPassword 新密码
     */
    public void forgetPassword(String email, String newPassword) {
        if (StringUtils.isBlank(email)) {
            throw new ServiceException("邮箱不能为空");
        }
        
        if (StringUtils.isBlank(newPassword)) {
            throw new ServiceException("新密码不能为空");
        }
        
        // 验证密码长度
        if (newPassword.length() < 6 || newPassword.length() > 100) {
            throw new ServiceException("密码长度必须在6到100个字符之间");
        }
        
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getEmail, email)
        );
        
        if (user == null) {
            throw new ServiceException("邮箱不存在，请检查后重试");
        }
        
        // 更新密码
        user.setPassword(bCryptPasswordEncoder.encode(newPassword));
        
        if (userMapper.updateById(user) <= 0) {
            throw new ServiceException("密码重置失败，请稍后再试");
        }
        
        // TODO: 在实际生产环境中，这里应该发送一封确认邮件到用户邮箱
        // sendPasswordResetConfirmationEmail(user.getEmail(), user.getUsername());
    }

    /**
     * 加密密码
     */
    public String encryptPassword(String password) {
        return bCryptPasswordEncoder.encode(password);
    }

    /**
     * 根据ID删除用户
     * 在删除前检查用户是否被医生或患者关联
     */
    @Transactional
    public void deleteUserById(Long id) {
        // 检查用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new ServiceException("用户不存在");
        }
        
        // 检查用户是否被医生关联
        Doctor doctor = doctorMapper.selectOne(
            new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getUserId, id)
        );
        if (doctor != null) {
            throw new ServiceException("该用户已关联医生信息（" + doctor.getName() + "），不能删除");
        }
        
        // 检查用户是否被患者关联
        Patient patient = patientMapper.selectOne(
            new LambdaQueryWrapper<Patient>()
                .eq(Patient::getUserId, id)
        );
        if (patient != null) {
            throw new ServiceException("该用户已关联患者信息（" + patient.getName() + "），不能删除");
        }
        
        // 执行删除操作
        if (userMapper.deleteById(id) <= 0) {
            throw new ServiceException("删除失败");
        }
    }
}
