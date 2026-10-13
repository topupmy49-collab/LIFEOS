package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.LifeOSDatabase
import com.example.data.local.entity.HabitCompletionEntity
import com.example.data.local.entity.HabitEntity
import com.example.data.local.entity.NoteEntity
import com.example.data.local.entity.TaskEntity
import com.example.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var database: LifeOSDatabase

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, LifeOSDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun testAppNameResource() {
        val appName = context.getString(R.string.app_name)
        assertEquals("LIFEOS", appName)
    }

    @Test
    fun testTaskDao_insertAndQuery() = runBlocking {
        val task = TaskEntity(
            title = "Ship MVP Release",
            description = "Complete Room, Compose, and Localization",
            priority = "HIGH"
        )
        val id = database.taskDao().insertTask(task)
        assertTrue(id > 0)

        val tasks = database.taskDao().getAllTasks().first()
        assertEquals(1, tasks.size)
        assertEquals("Ship MVP Release", tasks[0].title)
        assertEquals("HIGH", tasks[0].priority)
    }

    @Test
    fun testNoteDao_insertAndQuery() = runBlocking {
        val note = NoteEntity(
            title = "Life Architecture",
            content = "Keep simple, consistent daily discipline",
            isPinned = true
        )
        database.noteDao().insertNote(note)

        val notes = database.noteDao().getAllNotes().first()
        assertEquals(1, notes.size)
        assertTrue(notes[0].isPinned)
        assertEquals("Life Architecture", notes[0].title)
    }

    @Test
    fun testHabitCompletion_uniquenessConstraint() = runBlocking {
        val habit = HabitEntity(title = "Morning Meditation")
        val habitId = database.habitDao().insertHabit(habit)

        val record1 = HabitCompletionEntity(habitId = habitId, dateString = "2026-10-13")
        val id1 = database.habitDao().insertCompletion(record1)
        assertTrue(id1 > 0)

        // Inserting same habitId and dateString should be ignored due to IGNORE OnConflictStrategy
        val record2 = HabitCompletionEntity(habitId = habitId, dateString = "2026-10-13")
        val id2 = database.habitDao().insertCompletion(record2)
        // Room OnConflictStrategy.IGNORE returns -1 on duplicate
        assertEquals(-1L, id2)

        val list = database.habitDao().getCompletionListForHabit(habitId)
        assertEquals(1, list.size)
    }

    @Test
    fun testFinanceDao_insertAndTotals() = runBlocking {
        val salary = TransactionEntity(
            type = "INCOME",
            amountCents = 100000_00L,
            category = "Salary",
            transactionDateEpoch = System.currentTimeMillis()
        )
        val rent = TransactionEntity(
            type = "EXPENSE",
            amountCents = 35000_00L,
            category = "Housing & Rent",
            transactionDateEpoch = System.currentTimeMillis()
        )

        database.financeDao().insertTransaction(salary)
        database.financeDao().insertTransaction(rent)

        val all = database.financeDao().getAllTransactions().first()
        assertEquals(2, all.size)

        val totalIncome = all.filter { it.type == "INCOME" }.sumOf { it.amountCents }
        val totalExpense = all.filter { it.type == "EXPENSE" }.sumOf { it.amountCents }
        assertEquals(100000_00L, totalIncome)
        assertEquals(35000_00L, totalExpense)
    }
}
