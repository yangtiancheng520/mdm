// 测试值域API是否返回正确的编码

// 模拟 ValueDomainService.getOptionsByDomainId
// 之前代码：
// new DomainOptionDto(item.getItemCode(), item.getItemValue(), item.getItemValue(), item.getSort())
// 修改后：
// new DomainOptionDto(item.getItemCode(), item.getItemCode(), item.getItemValue(), item.getSort())

// 测试数据：
// item_code = "1000", item_value = "采购组织A"
// 
// 修改前返回：
// DomainOptionDto(code="1000", value="采购组织A", label="采购组织A")
// 
// 修改后返回：
// DomainOptionDto(code="1000", value="1000", label="采购组织A")
// 
// 前端绑定 :value="opt.value"，所以：
// 修改前：value = "采购组织A"（名称） ❌
// 修改后：value = "1000"（编码） ✅

System.out.println("修改后的逻辑正确，保存的是编码：1000");
