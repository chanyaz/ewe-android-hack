package com.expedia.bookings.itin.vm

import android.content.Context
import android.support.design.widget.TabLayout
import com.expedia.bookings.data.trips.TripHotelRoom
import com.expedia.bookings.itin.data.ItinCardDataHotel
import com.expedia.bookings.itin.widget.HotelItinManageRoomWidget
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

class HotelItinManageRoomViewModel(context: Context) : TabLayout.OnTabSelectedListener {
    //activity subjects
    val refreshItinCardDataSubject = BehaviorSubject.create<ItinCardDataHotel>()
    val closeActivitySubject = PublishSubject.create<Unit>()

    //tab and number of rooms text subjects
    val initializeTabsSubject = PublishSubject.create<Int>()
    val clearTabsSubject = PublishSubject.create<Unit>()
    val hideTabsSubject = PublishSubject.create<Unit>()
    val addTabSubject = PublishSubject.create<Int>()
    val showNumberOfRoomsTextSubject = PublishSubject.create<String>()
    val hideNumberOfRoomsTextSubject = PublishSubject.create<Unit>()

    //manage room widget subjects
    val roomDetailsSubject = PublishSubject.create<TripHotelRoom>()
    val roomChangeAndCancelRulesSubject = PublishSubject.create<List<String>>()
    val itinCardDataHotelSubject = PublishSubject.create<ItinCardDataHotel>()

    val manageRoomWidget by lazy {
        val widget = HotelItinManageRoomWidget(context, null)
        widget.viewModel = this
        widget
    }

    init {
        refreshItinCardDataSubject.subscribe {
            //create tabs
            val rooms = it.rooms
            val numberOfRooms = rooms.size
            when (numberOfRooms) {
                0 -> closeActivitySubject.onNext(Unit)
                1 -> {
                    hideTabsSubject.onNext(Unit)
                    hideNumberOfRoomsTextSubject.onNext(Unit)
                }
                else -> {
                    clearTabsSubject.onNext(Unit)
                    initializeTabsSubject.onNext(numberOfRooms)
                    showNumberOfRoomsTextSubject.onNext(numberOfRooms.toString())
                    createTabs(numberOfRooms)
                }
            }

            //show first room as default
            val room = it.getHotelRoom(0)
            setupRoom(room, it)
        }
    }

    fun setupRoom(room: TripHotelRoom?, it: ItinCardDataHotel) {
        if (room != null) {
            roomDetailsSubject.onNext(room)
            val changeAndCancelRules = it.changeAndCancelRules
            if (changeAndCancelRules != null && changeAndCancelRules.isNotEmpty()) {
                roomChangeAndCancelRulesSubject.onNext(changeAndCancelRules)
            }
            itinCardDataHotelSubject.onNext(it)
        } else {
            closeActivitySubject.onNext(Unit)
        }
    }

    fun createTabs(numberOfRooms: Int) {
        (0 until numberOfRooms).forEach {
            addTabSubject.onNext(it + 1)
        }
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        if (tab != null) {
            val tabPosition = tab.position
            val itinCardDataHotel = refreshItinCardDataSubject.value
            val room = itinCardDataHotel.getHotelRoom(tabPosition)
            setupRoom(room, itinCardDataHotel)
        }
    }
}
