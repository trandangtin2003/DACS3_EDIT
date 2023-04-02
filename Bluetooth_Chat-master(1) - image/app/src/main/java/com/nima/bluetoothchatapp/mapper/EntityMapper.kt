package com.nima.bluetoothchatapp.mapper
//Interface này cho phép các mapper class (class ánh xạ) triển khai và
// sử dụng chúng để thực hiện chuyển đổi dữ liệu giữa các lớp
// entity và domain một cách dễ dàng và tái sử dụng được.
interface EntityMapper<EntityMapper,DomainModel> {
    fun mapFromEntity(entity: EntityMapper): DomainModel?
    fun mapToEntity(domainModel: DomainModel): EntityMapper?
}