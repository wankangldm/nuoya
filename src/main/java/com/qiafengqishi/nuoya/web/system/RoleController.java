package com.qiafengqishi.nuoya.web.system;

import com.qiafengqishi.nuoya.domain.node.Node;
import com.qiafengqishi.nuoya.domain.node.ZTreeNode;
import com.qiafengqishi.nuoya.repository.dao.Role;
import com.qiafengqishi.nuoya.repository.dao.User;
import com.qiafengqishi.nuoya.repository.mapper.RoleMapper;
import com.qiafengqishi.nuoya.repository.mapper.UserMapper;
import com.qiafengqishi.nuoya.service.system.RoleService;
import com.qiafengqishi.nuoya.utils.Convert;
import com.qiafengqishi.nuoya.utils.StringUtil;
import com.qiafengqishi.nuoya.web.AjaxResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * author wankang 20200108
 */
@RestController
@RequestMapping("/role")
public class RoleController {
    private Logger logger = LoggerFactory.getLogger(RoleController.class);

    @Autowired
    private RoleService roleService;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * 获取角色列表
     * @param name 按名字搜索
     * @return
     */
    @RequestMapping(value = "/list",method = RequestMethod.GET)
    public Object list(String name){
        List roles = null;
        if(StringUtil.isNullOrEmpty(name)) {
            roles = roleMapper.findAll();
        }else{
            //带参查询
            roles = roleMapper.findByName("%"+name+"%");
        }
        return AjaxResult.success(roles);
    }

    /**
     * 保存角色
     * @param role
     * @return
     */
    @RequestMapping(value = "/save",method = RequestMethod.POST)
    public Object save(@Valid Role role){
        if(role.getId()==null) {
            roleMapper.insert(role);
        }else{
            roleMapper.updateByPrimaryKey(role);
        }
        return AjaxResult.success("保存成功");
    }

    /**
     * 删除角色
     * @param roleId
     * @return
     */
    @RequestMapping(value = "/remove",method = RequestMethod.DELETE)
    public Object remove(@RequestParam Long roleId){
        logger.info("id:{}",roleId);
        if (roleId==null) {
            return AjaxResult.failAlert("获取角色失败");
        }
        //不能删除超级管理员角色
        if(roleId.intValue() == 1){
            return AjaxResult.failAlert("禁止删除超级管理员角色");
        }
        //

        try{
            roleMapper.deleteByPrimaryKey(roleId);
            return AjaxResult.success("删除角色成功");
        }catch (Exception e){
            return AjaxResult.failAlert("删除角色失败");
        }

    }

    /**
     * 保存权限列表
     * @param roleId 角色id
     * @param permissions 菜单权限id 多个逗号隔开
     * @return
     */
    @RequestMapping(value = "/savePermisson",method = RequestMethod.POST)
    public Object savePermisson(Long roleId, String
            permissions) {
        if (roleId == null) {
            return AjaxResult.failAlert("保存失败,角色为空");
        }
        roleService.setAuthority(roleId, permissions);
        return AjaxResult.success("保存成功");
    }


    /**
     * 获取角色树
     * @param idUser
     * @return
     */
    @RequestMapping(value = "/roleTreeListByIdUser", method = RequestMethod.GET)
    public Object roleTreeListByIdUser(Long idUser) {
        User user = userMapper.selectByPrimaryKey(idUser);
        String roleIds = user.getRoleid();//获取用户角色
        List<ZTreeNode> roleTreeList = null;
        if (StringUtil.isEmpty(roleIds)) {
            roleTreeList = roleService.roleTreeList();
        } else {
            Long[] roleArray = Convert.toLongArray(",", roleIds);
            roleTreeList = roleService.roleTreeListByRoleId(roleArray);

        }
        List<Node> list = roleService.generateRoleTree(roleTreeList);
        List<Long> checkedIds = new ArrayList<Long>();
        for (ZTreeNode zTreeNode : roleTreeList) {
            if (zTreeNode.getChecked() != null && zTreeNode.getChecked()) {
                checkedIds.add(zTreeNode.getId());
            }
        }
        Map map = new HashMap();
        map.put("treeData",list);
        map.put("checkedIds",checkedIds);
        return AjaxResult.success(map);
    }


}
