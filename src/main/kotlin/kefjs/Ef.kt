package kefjs

import org.w3c.dom.HTMLElement
import org.w3c.dom.events.Event

class Ef {
    class EfPrepare {
        private var efprepare : dynamic
        constructor(tpl:String) {
            efprepare = js("(ef.create(tpl))")
        }
        constructor(ast: Array<Any>) {
            efprepare = js("(ef.create(ast))")
        }
        fun newInstance() : Ef = Ef(efprepare)

        fun newInstance(config:KefConfigModel): Ef = Ef(efprepare, config)
    }
    enum class EfOption(s: String) {
        REPLACE("replace"), APPEND("append")
    }
    private var instance : dynamic
    private val valueFuncMap = mutableMapOf<MethodFunction2, dynamic>()
    private val methodFuncMap = mutableMapOf<BaseMethodFunction, dynamic>()
    private val methodNameMap = mutableMapOf<String, BaseMethodFunction>()
    companion object {
        /*
         0 for production
         1 for debug
         */
        var infoLevel = 1

        fun create(tpl:String) = EfPrepare(tpl)

        fun create(ast:Array<Any>) = EfPrepare(ast)

        fun inform() = js("ef.inform()") as Int

        fun exec(force : Boolean = false) = js("ef.exec(force)") as Int

        fun bundle(func: BunbleFunction) : Int{
            inform()
            var result = false
            try {
                result = func.invoke()
            } finally {
                return exec(result)
            }
        }
        fun bundle(func: () -> Boolean) : Int{
            inform()
            var result = false
            try {
                result = func()
            } finally {
                return exec(result)
            }
        }

        fun onNextRender(func: MethodFunction) {
            js("ef.onNextRender(func.call)")
        }
        fun parseEft(tplast : String) = js("ef.parseEft(tplast)").unsafeCast<Array<dynamic>>()
        fun setParser(func : ParserFunction) {
            js("ef.setParser(func.call)")
        }
        fun setParser(func: (str: String)->Unit) {
            setParser(object : ParserFunction {
                override fun invoke(str: String) {
                    func(str)
                }
            })
        }

        //Kef
        private fun getKEf(ef : dynamic) : Ef = ef["\$k\$efjs"] as Ef
    }
    constructor(proto: EfPrepare) {
        instance = js("new proto")
        instance["\$k\$efjs"] = this
        instance["getKEf"] = js("function () { return this.\$k\$efjs }")
        data = KefData(instance)
    }
    constructor(proto: EfPrepare, config:KefConfigModel) {
        instance = js("new proto")
        instance["\$k\$efjs"] = this
        instance["getKEf"] = js("function () { return this.\$k\$efjs }")
        data = KefData(instance)
        Ef.bundle {
            config.data.forEach {
                data[it.arg] = it.data
            }
            config.method1.forEach {
                setMethod(it.name, it.func)
            }
            config.method2.forEach {
                setMethod(it.name, it.func)
            }
            config.method3.forEach {
                setMethod(it.name, it.func)
            }
            this.setOnMountListener(config.onMount)
            val isDebug = Ef.infoLevel
            Ef.infoLevel = 0 //Temporarily disable Ef status check
            config.mount.forEach {
                mount_calllistener(it.key, it.value)
            }
            Ef.infoLevel = isDebug //Restore Ef status check
            false
        }
    }
    //Init Mount
    @Deprecated("Use EfOption instead. Will remove in 0.7.3", ReplaceWith("mount(target, EfOption.REPLACE)", "kefjs.Ef.EfOption"))
    fun mount(target: HTMLElement?, option: String = "append") {
        mount(target, EfOption.valueOf(option))
    }
    fun mount(target: HTMLElement?, option: EfOption = EfOption.APPEND) {
        val option_str = when(option) {
            EfOption.APPEND -> "append"
            EfOption.REPLACE -> "replace"
        }
        instance.`$mount`(js("{target: target, option: option_str}"))
    }
    fun mount(target: HTMLElement?) {
        mount(target, EfOption.APPEND)
    }
    fun umount() {
        instance.`$umount`()
    }

    //Subscribe
    fun subscribe(name: String, func: MethodFunction2) {
        valueFuncMap.put(func, js("function (option) { func.call(option.state.\$k\$efjs, option.value) }"))
        instance.`$subscribe`(name,valueFuncMap[func])
    }
    fun subscribe(name: String, func: (state: Ef, value: String)-> Unit) {
        subscribe(name, object : MethodFunction2{
            override fun invoke(state: Ef, value: String) {
                func(state, value)
            }
        })
    }
    fun unsubscribe(name: String, func: MethodFunction2) {
        instance.`$unsubscribe`(name,valueFuncMap[func])
        valueFuncMap.remove(func)
    }
    //Data
    public val data : KefData

    @Deprecated("Use ef.data[arg] = data. Will remove in 0.7.3", ReplaceWith("data[arg] = data"))
    fun setData(arg: String, data: Any) {
        this.data[arg] = data
    }

    @Deprecated("Use ef.data[arg]. Will remove in 0.7.3", ReplaceWith("data[arg]"))
    fun getData(arg: String) = this.data[arg]

    //Mount
    @Deprecated("Use mount instead. Will remove in 0.7.3", ReplaceWith("mount(root, ef)"))
    fun subMount(root: String, ef: Ef?) {
        mount(root, ef)
    }
    fun mount(root: String, ef: Ef?) {
        if (ef == null) {
            instance[root] = null
        } else {
            instance[root] = ef.instance
        }
    }

    fun list(key: String) = KefList(key, instance)

    @Deprecated("Use ef.list(key)[position]. Will remove in 0.7.3", ReplaceWith("list(key)[position]"))
    fun listGet(key: String, position: Int) = KefList(key, instance)[position]

    @Deprecated("Use ef.list(key).push(ef). Will remove in 0.7.3", ReplaceWith("list(key).push(ef)"))
    fun listPush(key: String, efinstance: Ef) {
        KefList(key, instance).push(efinstance)
    }

    @Deprecated("Use ef.list(key).remove(position). Will remove in 0.7.3", ReplaceWith("list(key).remove(position)"))
    fun listRemove(key: String, position: Int) {
        KefList(key, instance).remove(position)
    }

    @Deprecated("Use ef.list(key).empty(). Will remove in 0.7.3", ReplaceWith("list(key).empty()"))
    fun listEmpty(key: String) {
        KefList(key, instance).empty()
    }

    @Deprecated("Use ef.list(key).size(). Will remove in 0.7.3", ReplaceWith("list(key).size()"))
    fun listSize(key: String) = KefList(key, instance).size()

    //Refs
    fun getRefs(name:String) = instance.`$refs`[name] as HTMLElement

    //Parent
    fun getParent() = instance.`$parent`

    //Key
    fun getKey() = instance.`$key` as String

    //Element
    fun getElement() = instance.`$element` as HTMLElement

    //EFPLACEHOLDER
    fun debugEFPLACEHOLDER() = instance.__EFPLACEHOLDER__ as HTMLElement

    //Methods
    fun setMethod(name: String, func: MethodFunction1) {
        if (!methodFuncMap.containsKey(func)) methodFuncMap.put(func, js("function (option) { func.call(option.state.\$k\$efjs) }"))
        methodNameMap.put(name, func)
        instance.`$methods`[name] = methodFuncMap[func]
    }
    fun setMethod(name: String, func: MethodFunction2) {
        if (!methodFuncMap.containsKey(func)) methodFuncMap.put(func, js("function (option) { func.call(option.state.\$k\$efjs, option.value) }"))
        methodNameMap.put(name, func)
        instance.`$methods`[name] = methodFuncMap[func]
    }
    fun setMethod(name: String, func: MethodFunction3) {
        if (!methodFuncMap.containsKey(func)) methodFuncMap.put(func, js("function (option) { func.call(option.state.\$k\$efjs, option.value, option.e) }"))
        methodNameMap.put(name, func)
        instance.`$methods`[name] = methodFuncMap[func]
    }
    fun setMethod(name: String, func: (state: Ef) -> Unit) {
        setMethod(name, object : MethodFunction1 {
            override fun invoke(state: Ef) {
                func(state)
            }
        })
    }
    fun setMethod(name: String, func: (state: Ef, value: String) -> Unit) {
        setMethod(name, object : MethodFunction2 {
            override fun invoke(state: Ef, value: String) {
                func(state, value)
            }
        })
    }
    fun setMethod(name: String, func: (state: Ef, value: String, e: Event) -> Unit) {
        setMethod(name, object : MethodFunction3 {
            override fun invoke(state: Ef, value: String, e: Event) {
                func(state, value, e)
            }
        })
    }
    fun <T> getMethod(name: String) = methodNameMap[name].unsafeCast<T>()


    //Store
    private val userStoreMap = mutableMapOf<String, Any>()
    fun editUserStore(key: String, value: Any?) {
        if(value == null) {
            userStoreMap.remove(key)
        } else {
            userStoreMap.put(key, value)
        }
    }
    fun <T> getUserStore(key: String, default: T) : T = if (userStoreMap[key] == null) default else userStoreMap[key].unsafeCast<T>()

    //Raw
    fun getInstance() = instance

    //Interface
    public interface ParserFunction {
        @JsName("call")
        operator fun invoke(str: String)
    }
    interface BaseMethodFunction { }
    public interface BunbleFunction {
        @JsName("call")
        operator fun invoke() : Boolean
    }
    public interface MethodFunction : BaseMethodFunction{
        @JsName("call")
        operator fun invoke()
    }
    public interface MethodFunction1 : BaseMethodFunction{
        @JsName("call")
        operator fun invoke(state: Ef)
    }
    public interface MethodFunction2 : BaseMethodFunction{
        @JsName("call")
        operator fun invoke(state: Ef, value: String)
    }
    public interface MethodFunction3 : BaseMethodFunction{
        @JsName("call")
        operator fun invoke(state: Ef, value: String, e : Event)
    }

    //Helper
    class KefList(val key: String, val instance: dynamic) {
        operator fun get(position: Int): Ef? = instance[key][position]["\$k\$efjs"] as Ef
        fun push(efinstance: Ef) = instance[key].push(efinstance.instance) as Int
        fun push(vararg efinstances: Ef) : Int {
            Ef.bundle(object : BunbleFunction {
                override fun invoke(): Boolean {
                    efinstances.forEach { push(it) }
                    return false
                }
            })
            return size()
        }

        fun remove(position: Int) {
            instance[key].remove(position)
        }
        fun empty() {
            instance[key].empty()
        }
        fun size() = instance[key].length as Int
    }

    class KefData(val instance: dynamic) {
        operator fun get(arg: String) = instance.`$data`[arg] as Any
        operator fun <T> get(arg: String) = instance.`$data`[arg].unsafeCast<T>()
        operator fun set(arg: String, data:Any) {
            instance.`$data`[arg] = data
        }
    }

}

fun String.prepareEf() : Ef.EfPrepare = Ef.create(this)
fun String.instanceEf() : Ef = this.prepareEf().newInstance()
fun String.instanceEf(config:KefConfigModel) : Ef = this.prepareEf().newInstance(config)
