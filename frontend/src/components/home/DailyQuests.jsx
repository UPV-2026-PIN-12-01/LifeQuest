export default function DailyQuests({ quests }) {
  const claimed = quests.filter((quest) => quest.progress >= quest.goal).length

  return (
    <section className="quests-card" aria-labelledby="quests-title">
      <header className="quests-header">
        <h2 id="quests-title" className="quests-title">
          Misiones de hoy
        </h2>
        <span className="quests-count tabular">
          {claimed} / {quests.length}
        </span>
      </header>
      <ul className="quests-list">
        {quests.map((quest) => {
          const isClaimed = quest.progress >= quest.goal
          const percent = Math.min(100, Math.round((quest.progress / quest.goal) * 100))
          return (
            <li key={quest.id} className={`quest quest-color-${quest.color}${isClaimed ? ' is-claimed' : ''}`}>
              <span className="quest-icon" aria-hidden="true">{quest.icon}</span>
              <div className="quest-body">
                <p className="quest-title">{quest.title}</p>
                <div
                  className="quest-progress"
                  role="progressbar"
                  aria-label={quest.title}
                  aria-valuemin={0}
                  aria-valuemax={quest.goal}
                  aria-valuenow={quest.progress}
                >
                  <span className="quest-progress-fill" style={{ width: `${percent}%` }} />
                </div>
              </div>
              <span className="quest-xp tabular">
                {isClaimed ? '✓ ' : '+'}
                {quest.xp}
              </span>
            </li>
          )
        })}
      </ul>
    </section>
  )
}
