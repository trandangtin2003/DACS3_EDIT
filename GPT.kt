Yêu cầu : tôi muốn sử dụng ref_ad = dataBase.ref?.orderByChild("id_user")?.equalTo(id[1])!! tham chiếu tới note User_nhomHP với id_user bằng với giá trị id[1]                                                                                                    thông qua id_nhomHp ( của note : User_nhomHP) in ra tương ứng : id_nhomHp và hp(của note : Hoc_phan) và  nhom_hp(của note : nhom_HP)  vào trong file adapter.kt thông qua model.kt    .          
 Chỉ cho tôi cần phải thêm code gì vào trong từng file để thực hiện yêu cầu  , không cần giải thích kỹ chỉ cho tôi code để thực hiện yêu cầu của tôi    (bạn luôn trả lời thiếu //setup adapter
                    Adapter_ad = Adapter_ad(this@DashboardAdminActivity, hpArrayList)
                    //set adapter to recylerView
                    binding.categoriesRv.adapter = Adapter_ad nên Adapter không chạy được làm ơn hãy trả lời chính xác và ngắn gọn                                                                                                                                                          
        tôi có các note trong realtime database như sau:                                                                                                                           Users
    jwzcpuSyYXVOySAROPPV3dSP9C92
       email:"teacher1@gmail.com"
        id_user:"1681568238079"
        name:"teacher1@gmail.com"
        timestamp:1681568238079
Hoc_phan
     1681568277397
          hp:"lập trình di động 1"
          hpLink_spreadsheet:"http://"
          id_hp:"1681568277397"
          timestamp:1681568277397
nhom_HP
         1681582815080
          id_hp:"1681568277397"
          nhom_hp:"1"
         timestamp:1681582815080
User_nhomHP
     1682074007018
          id_nhomHp:"1681582815080"
          id_user:"jwzcpuSyYXVOySAROPPV3dSP9C92"
          timestamp:1682074007018


                                                                                                                                                                                                                                                                                                                                                                                                                                                                
                                                                                                                                                                                                                                                                                                                            theo code kotlin sau :                                                                                                                                                                                                                                        tệp dataBase.kt                                                                                                                                                                                                                                                                                                                     object dataBase {
    //lưu holder vừa click vào

    var id = mutableListOf<String>()
    val subTitle_list = mutableListOf<String>()

    var users_start : Boolean = false
    var users : Boolean = false
    var users_to_Hp:Boolean = false
    val nhomHp_list: MutableList<String> = mutableListOf()
    val nhomHp_list_khongtrung: MutableList<String> = mutableListOf()


    val ref_data = arrayOf(
        FirebaseDatabase.getInstance().getReference("Users"),
        FirebaseDatabase.getInstance().getReference("Hoc_phan"),
        FirebaseDatabase.getInstance().getReference("nhom_HP"),
        FirebaseDatabase.getInstance().getReference("buoi_Hoc"),
        FirebaseDatabase.getInstance().getReference("User_nhomHP")
    )
    var id_arr_ref:Int = 1
    set(value) {
        field = value
        ref = ref_data[value]
        dashboardAdminActivity?.doi_icon_transfer()
    }

    var ref: DatabaseReference? = null
        set(value) {
            field = value
            dashboardAdminActivity?.load()
        }
    var dashboardAdminActivity: DashboardAdminActivity? = null
    var classAct: Class<*> = HpAddActivity::class.java
}                                                                                                                                                                                                                               tệp DashboardAdminActivity.kt                                                                                                                                                                                                                                                                class DashboardAdminActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardAdminBinding

    //firebase auth
    private lateinit var firebaseAuth: FirebaseAuth

    /*progress dialog*/
    private lateinit var progressDialog: ProgressDialog

    //arayList to hold categories
    private lateinit var hpArrayList: ArrayList<Model_ad>

    // adapter
    private lateinit var Adapter_ad: Adapter_ad

    //xác định data
    private lateinit var ref_ad : Query

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*init firebase Auth*/
        firebaseAuth = FirebaseAuth.getInstance()
        //configure progress dialog
        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)
        checkUser()

        dataBase.dashboardAdminActivity = this

        ref = FirebaseDatabase.getInstance().getReference("Hoc_phan")
        //load()

        /*Search*/
        binding.searchEt.addTextChangedListener(object : TextWatcher {


            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //called as and when user type anything
                try {
                    Adapter_ad.filter.filter(s)
                    Log.d("Search", "Filtering with query: $s")
                } catch (e: Exception) {

                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })
        binding.subTitleTv.text = "quản lý Học phần"

        /*handle click , layout*/
        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }


/*chuyển đổi giữa học phần và user*/
        var clickCount = 0
        binding.transfer.setOnClickListener {



            if (users_to_Hp == true){
                users_to_Hp = false
                nhomHp_list.clear()
                id_arr_ref--
            }
            else if (users_to_Hp == false && users == true){
                users = false
                dataBase.nhomHp_list_khongtrung.clear()
                id_arr_ref = 4

            }
            else if (id_arr_ref == 4 && users_start == true){
                users_start = false
                id_arr_ref = 0
                id.clear()
                dataBase.nhomHp_list_khongtrung.clear()
                clickCount = 0
            }

            subTitle_list?.removeLastOrNull()
            id?.removeLastOrNull()


            clickCount++
            if (id_arr_ref > 1 && users_start == false) {
                id_arr_ref--
                clickCount++

            } else if (clickCount % 2 != 0 && users_start == false) {
//                binding.transfer.setImageResource(R.drawable.ic_supervised_user_circle_green)
                id_arr_ref = 0
//                ref = FirebaseDatabase.getInstance().getReference("Users")
                //load()
                //Trường hợp 1 :
            } else if (clickCount % 2 == 0 && users_start == false) {

                // binding.transfer.setImageResource(R.drawable.ic_edit_calendar_green)
                id_arr_ref = 1
//                ref = FirebaseDatabase.getInstance().getReference("Hoc_phan")
                //load()
                //Trường hợp 2 :
            }

            Log.i("id_ref trans", "$id_arr_ref, ref: $ref , cloick count : $clickCount , id : ${id.lastOrNull()}")

        }

        //handle click , start add hp page
        binding.addBtn.setOnClickListener {

            if (id_arr_ref==4){
                users = true
                id_arr_ref =1
            }else startActivity(Intent(this, classAct))
        }

        binding.addNhomHpUser.setOnClickListener {
            //show progress
            progressDialog.show()

            for (nhomHp in dataBase.nhomHp_list) {
                //get timestamp
                val timestamp = System.currentTimeMillis()

                //setup data to add in firebase db
                val hashMap =
                    HashMap<String, Any?>() //secon param is any ; because the value could be of any type

                hashMap["id_user"] = id[1]
                hashMap["id_nhomHp"] = nhomHp
                hashMap["timestamp"] = timestamp
                hashMap["uid"] = "${firebaseAuth.uid}"

                //add to firebase db: Database Root > hp > hpId > hp info
                val ref = FirebaseDatabase.getInstance().getReference("User_nhomHP")
                ref.child("$timestamp")
                    .setValue(hashMap)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Added succesfuly ... ", Toast.LENGTH_SHORT).show()
                        id_arr_ref --
                        nhomHp_list.clear()
                        users_to_Hp = false
                        binding.addNhomHpUser.visibility = View.GONE
                        binding.addBtn.visibility = View.VISIBLE
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(
                            this,
                            "Failed to add due to ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }


    }

    fun load() {
        //init arrayList
        hpArrayList = ArrayList()
        if (id_arr_ref == 2){
            ref_ad = dataBase.ref?.orderByChild("id_hp")?.equalTo(id.last())!!
        }
        else if (id_arr_ref == 3){
            ref_ad = dataBase.ref?.orderByChild("id_nhomHP")?.equalTo(id.last())!!
        }
//        else if(id_arr_ref == 4){
//            ref_ad = dataBase.ref?.orderByChild("id_user")?.equalTo(id[1])!!
//        }
        else ref_ad = ref!!
        Log.e("ref_ad : ","$ref_ad")
        //get all categories from firebase database ... Firebase DB > categories
//        val ref = FirebaseDatabase.getInstance().getReference("Hoc_phan")
        if (id_arr_ref != 4){
            ref_ad?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    //clear list before starting adding data into it
                    hpArrayList.clear()
                    for (ds in snapshot.children) {
                        //get data as model
                        //getValue() là một phương thức để chuyển đổi dữ liệu trong DataSnapshot thành đối tượng Model_ad trong ứng dụng
                        val model = ds.getValue(Model_ad::class.java)

                        //add to arrayList
                        hpArrayList.add(model!!)
                    }
                    //setup adapter
                    Adapter_ad = Adapter_ad(this@DashboardAdminActivity, hpArrayList)
                    //set adapter to recylerView
                    binding.categoriesRv.adapter = Adapter_ad
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        }
        else if (id_arr_ref == 4){


        }
    }                                                                                                                                                                                                       
  tệp Adapter_ad.kt                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          class Adapter_ad : RecyclerView.Adapter<Adapter_ad.Holder> , Filterable{
    private val context: Context
    public var ArrayList: ArrayList<Model_ad>



    /* filterList là danh sách các mục cần lọc*/
    private var filterList: ArrayList<Model_ad>
    /*filter là đối tượng Filter_ad sẽ được sử dụng để thực hiện tìm kiếm và lọc.*/
    private var filter: Filter_ad? = null



    private lateinit var binding: RowItemBinding


    var dashboardAdminActivity: DashboardAdminActivity? = null

    var count_checked = 0


    //constructor
    constructor(context: Context, ArrayList: ArrayList<Model_ad>) {
        this.context = context
        this.ArrayList = ArrayList
        this.filterList = ArrayList
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        //inflate / bind row_hp.xml
        /*inflate file layout row_hp.xml bằng phương thức inflate() trong đối tượng LayoutInflater*/
        binding = RowItemBinding.inflate(LayoutInflater.from(context),parent,false)

        return Holder(binding.root)

    }



    override fun onBindViewHolder(holder: Holder, position: Int) {
        /*get data , set data , handle click etc */
//binding.checkBox.visibility = View.VISIBLE
        //get data
        val model = ArrayList[position]
        val id = model.id_hp?.takeIf { it.isNotBlank() } ?:model.id_user

        val uid = model.uid
        val timestamp = model.timestamp


        val hp = model.hp
        val name = model.name
        val nhom_hp =   model.nhom_hp
        val ngay_hoc = model.ngay_hoc
        val h = model.h
        var id_nhomHp = model.id_nhomHp
        Log.d("kt","$name , id : $id ,uid : $uid,nhom_hp : $nhom_hp,timetamp : $timestamp,hp : $hp,ngay học : $ngay_hoc , h : $h , id_nhomHp_user : $id_nhomHp")

        if (id_arr_ref ==4 ) dataBase.nhomHp_list_khongtrung.add(id_nhomHp)
        Log.e("id_nhomHP không trùng :"," ${nhomHp_list_khongtrung.joinToString(",")}")
        //set data

        holder.Tv.text = name?.takeIf { it.isNotBlank() } ?: hp ?.takeIf { it.isNotBlank() } ?:nhom_hp?.takeIf { it.isNotBlank() } ?:ngay_hoc?.takeIf { it.isNotBlank() } ?:h?.takeIf { it.isNotBlank() } ?:id_nhomHp
        if (id_arr_ref == 4 ){

            holder.Tv.text = hp + nhom_hp
        }
        if (users == true) binding.deleteBtn.visibility = View.GONE
         if (users_to_Hp == true){
            Log.e("đã tới","bật check box")
             if (nhomHp_list_khongtrung.contains(timestamp.toString())) {
                 binding.checkBox.visibility = View.GONE
             }else binding.checkBox.visibility = View.VISIBLE
             binding.rowLl.isFocusable = true
             binding.rowLl.isFocusableInTouchMode = true
        }


        holder.rowLl.setOnClickListener{
            //a

            if (id_arr_ref >= 2){
                dataBase.id.add("${timestamp.toString()}")
            }else dataBase.id.add("${model.id_hp}")
            dataBase.subTitle_list.add("${holder.Tv.text}")

            if (id_arr_ref == 0){
                users_start = true
                dataBase.id.add("$uid")
                id_arr_ref=4
            }
            else if (users == true){
                users_to_Hp = true
                id_arr_ref++
            }

            else id_arr_ref ++

//            dataBase.ref = FirebaseDatabase.getInstance().getReference("nhom_HP")

            Log.i("id_ref","$id_arr_ref, ref: ${dataBase.ref} , name : ${dataBase.subTitle_list.joinToString(" | ")},nhóm hp : $timestamp,id_hp : ${model.id_hp}, id được truyền : ${dataBase.id.last()}, id trong danh sách : ${dataBase.id.joinToString(", ")}")
        }
Tệp Model_ad.kt                                                                                                                                                                                                                            class Model_ad {

    //hp
    var id_hp:String = ""
    var hp:String =""
    var hpLink:String =""
    var timestamp:Long = 0
    var uid:String =""
    //user
    var id_user:String = ""
    var email:String = ""
    var name: String = ""
    //nhóm học phần

    var nhom_hp:String = ""
    //buổi học
    var ngay_hoc:String = ""
    var h :String =""
    //user _ nhomHp
    var id_nhomHp: String =""


    //hàm tạo trống, được yêu cầu bởi firebase
    constructor()

    //hàm tạo được tham số hóa
    constructor(id: String,
                hp: String,hpLink: String,
                email:String,name:String,
                nhom_hp:String,
                ngay_hoc:String,h :String,
                id_nhomHp:String,
                timestamp: Long, uid: String) {
        this.id_hp = id
        this.hp = hp
        this.hpLink = hpLink


        this.id_user = id
        this.name = name
        this.email = email

        this.id_nhomHp = id_nhomHp
        this.nhom_hp = nhom_hp

        this.ngay_hoc= ngay_hoc
        this.h= h

        this.timestamp = timestamp
        this.uid = uid

    }

