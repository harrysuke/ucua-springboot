package com.ucua.ucua.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "ucuaform")
data class UcuaForm(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    val id: Int? = null,

    @Column(name = "companyCode", length = 3, nullable = false)
    val companyCode: String = "",

    @Column(name = "branchCode", length = 3, nullable = false)
    val branchCode: String = "",

    @Column(name = "LocationId", length = 20, nullable = false)
    val locationId: String = "",

    @Column(name = "OffenceCode", length = 255)
    val offenceCode: String? = null,

    @Column(name = "staffId", length = 30, nullable = false)
    val staffId: String = "",

    @Column(name = "evidenceImage", length = 255, nullable = false)
    val evidenceImage: String = "",

    @Column(name = "ActionTaken", length = 900)
    val actionTaken: String? = null,

    @Column(name = "otherValue", nullable = false, columnDefinition = "TEXT")
    val otherValue: String = "",

    @Column(name = "HSEReps", length = 30)
    val hseReps: String? = null,

    @Column(name = "HSEReps0", length = 30, nullable = false)
    val hseReps0: String = "",

    @Column(name = "hsereps1", length = 50, nullable = false)
    val hseReps1: String = "",

    @Column(name = "hsereps2", length = 50, nullable = false)
    val hseReps2: String = "",

    @Column(name = "relatedDept", nullable = false)
    val relatedDept: Int = 0,

    @Column(name = "conditionDetails", columnDefinition = "TEXT")
    val conditionDetails: String? = null,

    @Column(name = "followup", columnDefinition = "TEXT")
    val followUp: String? = null,

    @Column(name = "fineamount", nullable = false, precision = 6, scale = 2)
    val fineAmount: BigDecimal = BigDecimal.ZERO,

    @Column(name = "briefingFrom", nullable = false)
    val briefingFrom: LocalDate = LocalDate.now(),

    @Column(name = "briefingUntil", nullable = false)
    val briefingUntil: LocalDate = LocalDate.now(),

    @Column(name = "bannedMonths", nullable = false)
    val bannedMonths: LocalDate = LocalDate.now(),

    @Column(name = "bannedMonths2", nullable = false)
    val bannedMonths2: LocalDate = LocalDate.now(),

    @Column(name = "otherAction", nullable = false, columnDefinition = "TEXT")
    val otherAction: String = "",

    @Column(name = "form_type", length = 50, nullable = false)
    val formType: String = "",

    @Column(name = "observerName", length = 50, nullable = false)
    val observerName: String = "",

    @Column(name = "observerDepartment", length = 50, nullable = false)
    val observerDepartment: String = "",

    @Column(name = "observerEmail", length = 50, nullable = false)
    val observerEmail: String = "",

    @Column(name = "observerConditionDetails", nullable = false, columnDefinition = "TEXT")
    val observerConditionDetails: String = "",

    @Column(name = "observerDatetime", nullable = false)
    val observerDateTime: LocalDateTime = LocalDateTime.now(),

    @Column(name = "ViolatorName", length = 50)
    val violatorName: String? = null,

    @Column(name = "violatorContactNo", length = 25, nullable = false)
    val violatorContactNo: String = "",

    @Column(name = "ViolatorDept", length = 50)
    val violatorDept: String? = null,

    @Column(name = "violatorCompany", length = 100, nullable = false)
    val violatorCompany: String = "",

    @Column(name = "ViolatorIc", length = 20)
    val violatorIc: String? = null,

    @Column(name = "ViolatorPass", nullable = false, columnDefinition = "TEXT")
    val violatorPass: String = "",

    @Column(name = "violatorDatetime", nullable = false)
    val violatorDateTime: LocalDateTime = LocalDateTime.now(),

    @Column(name = "submitdate")
    val submitDate: LocalDateTime? = null,

    @Column(name = "cipissued_date", length = 50)
    val cipIssuedDate: String? = null,

    @Column(name = "actiontaken_date")
    val actionTakenDate: LocalDateTime? = null,

    @Column(name = "ActionTakenBy", length = 30)
    val actionTakenBy: String? = null,

    @Column(name = "reviewedby_date")
    val reviewedByDate: LocalDateTime? = null,

    @Column(name = "HOS", length = 200)
    val hos: String? = null,

    @Column(name = "approvedby_date")
    val approvedByDate: LocalDateTime? = null,

    @Column(name = "GM", length = 200)
    val gm: String? = null,

    @Column(name = "evidenceImage2", length = 255)
    val evidenceImage2: String? = null,

    @Column(name = "status1", length = 20, nullable = false)
    val status1: String = "",

    @Column(name = "status2", length = 20, nullable = false)
    val status2: String = "",

    @Column(name = "status3", length = 20, nullable = false)
    val status3: String = "",

    @Column(name = "status4", length = 20, nullable = false)
    val status4: String = "",

    @Column(name = "ImgEvidence", length = 20)
    val imgEvidence: String? = null,

    @Column(name = "remarkImgEvidence", length = 200)
    val remarkImgEvidence: String? = null,

    @Column(name = "status5", length = 20)
    val status5: String? = null,

    @Column(name = "status6", length = 20, nullable = false)
    val status6: String = "",

    @Column(name = "status7", length = 20, nullable = false)
    val status7: String = "",

    @Column(name = "status", length = 255, nullable = false)
    val status: String = "",

    @Column(name = "createby", length = 20)
    val createBy: String? = null,

    @Column(name = "createdate")
    val createDate: LocalDateTime? = null,

    @Column(name = "editby", length = 20)
    val editBy: String? = null,

    @Column(name = "editdate")
    val editDate: LocalDateTime? = null,

    @Column(name = "isdelete", nullable = false)
    val isDelete: Int = 0,

    @Column(name = "deleteby", length = 20, nullable = false)
    val deleteBy: String = "",

    @Column(name = "deletedate", nullable = false)
    val deleteDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "isUserClosed", nullable = false)
    val isUserClosed: Int = 0,

    @Column(name = "userClosedBy", length = 20, nullable = false)
    val userClosedBy: String = "",

    @Column(name = "userClosedDate", nullable = false)
    val userClosedDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "isverified", nullable = false)
    val isVerified: Int = 0,

    @Column(name = "verifiedby", length = 20)
    val verifiedBy: String? = null,

    @Column(name = "verifieddate")
    val verifiedDate: LocalDateTime? = null,

    @Column(name = "isfollowup", nullable = false)
    val isFollowUp: Int = 0,

    @Column(name = "followupby", length = 20)
    val followUpBy: String? = null,

    @Column(name = "followupdate")
    val followUpDate: LocalDateTime? = null,

    @Column(name = "isapproved", nullable = false)
    val isApproved: Int = 0,

    @Column(name = "approvedby", length = 20)
    val approvedBy: String? = null,

    @Column(name = "approveddate")
    val approvedDate: LocalDateTime? = null,

    @Column(name = "isrejected", nullable = false)
    val isRejected: Int = 0,

    @Column(name = "rejectedby", length = 20)
    val rejectedBy: String? = null,

    @Column(name = "rejecteddate")
    val rejectedDate: LocalDateTime? = null,

    @Column(name = "isHOSrejected", nullable = false)
    val isHosRejected: Int = 0,

    @Column(name = "HOSrejectedby", length = 20, nullable = false)
    val hosRejectedBy: String = "",

    @Column(name = "HOSrejecteddate", nullable = false)
    val hosRejectedDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "isHODrejected", nullable = false)
    val isHodRejected: Int = 0,

    @Column(name = "HODrejectedby", length = 20, nullable = false)
    val hodRejectedBy: String = "",

    @Column(name = "HODrejecteddate", nullable = false)
    val hodRejectedDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "isnotifysherep", nullable = false)
    val isNotifySheRep: Int = 0,

    @Column(name = "notifysherepby", length = 20, nullable = false)
    val notifySheRepBy: String = "",

    @Column(name = "notifysherepdate", nullable = false)
    val notifySheRepDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "isupload", nullable = false)
    val isUpload: Int = 0,

    @Column(name = "uploadby", length = 20)
    val uploadBy: String? = null,

    @Column(name = "uploaddate")
    val uploadDate: LocalDateTime? = null,

    @Column(name = "isconfirm", nullable = false)
    val isConfirm: Int = 0,

    @Column(name = "confirmby", length = 20)
    val confirmBy: String? = null,

    @Column(name = "confirmdate")
    val confirmDate: LocalDateTime? = null,

    @Column(name = "isNotifyHos", nullable = false)
    val isNotifyHos: Int = 0,

    @Column(name = "notifyHosBy", length = 20)
    val notifyHosBy: String? = null,

    @Column(name = "notifyHosDate")
    val notifyHosDate: LocalDateTime? = null,

    @Column(name = "isReviewed", nullable = false)
    val isReviewed: Int = 0,

    @Column(name = "reviewedBy", length = 20, nullable = false)
    val reviewedBy: String = "",

    @Column(name = "reviewedDate", nullable = false)
    val reviewedDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "isReassign", nullable = false)
    val isReassign: Int = 0,

    @Column(name = "reassignBy", length = 20, nullable = false)
    val reassignBy: String = "",

    @Column(name = "reassignDate", nullable = false)
    val reassignDate: LocalDateTime = LocalDateTime.now()
)
