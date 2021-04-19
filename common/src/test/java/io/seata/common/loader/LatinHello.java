package io.seata.common.loader;

/**
 * @author: xielongfei
 * @date: 2021/03/21 20:30
 * @description:
 */
@LoadLevel(name = "LatinHello", order = 3, scope = Scope.PROTOTYPE)
public class LatinHello implements Hello{

    @Override
    public String say() {
        return "Olá.";
    }
}
