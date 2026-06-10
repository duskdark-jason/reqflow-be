-- REQ-003 统一需求流转平台品牌清理

update sys_dept
set dept_name = '统一需求流转平台', leader = '平台管理员', email = 'admin@reqflow.local'
where dept_id = 100 and dept_name like '%若依%';

update sys_dept
set leader = '平台管理员', email = 'admin@reqflow.local'
where leader = '若依' or email in ('ry@qq.com', 'ry@163.com');

update sys_user
set nick_name = '平台管理员', email = 'admin@reqflow.local'
where user_id = 1 and nick_name = '若依';

update sys_user
set nick_name = '测试人员', email = 'tester@reqflow.local'
where user_id = 2 and nick_name = '若依';

delete from sys_menu
where menu_name = '若依官网' or path in ('http://ruoyi.vip', 'http://www.ruoyi.vip');

delete from sys_notice
where notice_title like '%若依%' or notice_content like '%ruoyi.vip%' or notice_content like '%RuoYi%';
